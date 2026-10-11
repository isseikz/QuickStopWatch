package com.puzzroom.quickstopwatch.service

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.puzzroom.quickstopwatch.data.OverlayPositionRepository
import com.puzzroom.quickstopwatch.data.Position
import com.puzzroom.quickstopwatch.domain.StopwatchEngine
import com.puzzroom.quickstopwatch.theme.QuickStopWatchTheme
import com.puzzroom.quickstopwatch.ui.floating.FloatingStopwatch
import com.puzzroom.quickstopwatch.ui.floating.FloatingStopwatchSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

/** Manages the overlay Window, ComposeView lifecycle, position drag and keep-screen-on (DD-006). */
class StopwatchOverlayController(
  private val context: Context,
  private val windowManager: WindowManager =
    context.getSystemService(Context.WINDOW_SERVICE) as WindowManager,
  val engine: StopwatchEngine,
  private val positionRepository: OverlayPositionRepository = OverlayPositionRepository(context),
) {
  private var overlayView: View? = null
  private var scope: CoroutineScope? = null
  private var currentPosition: Position = Position(0, 0)
  private lateinit var layoutParams: WindowManager.LayoutParams

  val isShowing: Boolean
    get() = overlayView != null

  fun show() {
    if (isShowing) return

    val metrics = context.resources.displayMetrics
    val screenWidth = metrics.widthPixels
    val screenHeight = metrics.heightPixels
    val sizePx = (FloatingStopwatchSize.value * metrics.density).toInt()

    currentPosition = positionRepository.loadPosition(screenWidth, screenHeight, sizePx)

    layoutParams =
      WindowManager.LayoutParams(
          WindowManager.LayoutParams.WRAP_CONTENT,
          WindowManager.LayoutParams.WRAP_CONTENT,
          WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
          // REQ-010: FLAG_KEEP_SCREEN_ON, non-focusable to allow touching apps underneath
          WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
          PixelFormat.TRANSLUCENT,
        )
        .apply {
          gravity = Gravity.TOP or Gravity.START
          x = currentPosition.x
          y = currentPosition.y
        }

    val composeView =
      ComposeView(context).apply {
        val lifecycleOwner = OverlayLifecycleOwner()
        lifecycleOwner.performRestore(null)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        setViewTreeLifecycleOwner(lifecycleOwner)
        setViewTreeViewModelStoreOwner(lifecycleOwner)
        setViewTreeSavedStateRegistryOwner(lifecycleOwner)

        setContent {
          QuickStopWatchTheme {
            val state by engine.uiState.collectAsState()
            FloatingStopwatch(
              state = state,
              onTap = { engine.toggle() },
              onDrag = { delta: Offset ->
                updatePosition(delta, screenWidth, screenHeight, sizePx)
              },
              onDragEnd = {
                positionRepository.savePosition(currentPosition)
              },
            )
          }
        }
      }

    windowManager.addView(composeView, layoutParams)
    overlayView = composeView
  }

  fun hide() {
    if (!isShowing) return
    engine.reset() // REQ-009: Hiding resets stopwatch
    scope?.cancel()
    scope = null

    overlayView?.let { view ->
      try {
        windowManager.removeView(view)
      } catch (_: Exception) {}
    }
    overlayView = null
  }

  private fun updatePosition(delta: Offset, screenWidth: Int, screenHeight: Int, sizePx: Int) {
    val newX =
      (currentPosition.x + delta.x.toInt()).coerceIn(0, (screenWidth - sizePx).coerceAtLeast(0))
    val newY =
      (currentPosition.y + delta.y.toInt()).coerceIn(0, (screenHeight - sizePx).coerceAtLeast(0))
    currentPosition = Position(newX, newY)

    layoutParams.x = currentPosition.x
    layoutParams.y = currentPosition.y
    overlayView?.let { view ->
      try {
        windowManager.updateViewLayout(view, layoutParams)
      } catch (_: Exception) {}
    }
  }

  fun getLayoutParams(): WindowManager.LayoutParams? = if (isShowing) layoutParams else null

  private class OverlayLifecycleOwner :
    LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle
      get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
      get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
      get() = store

    fun handleLifecycleEvent(event: Lifecycle.Event) = lifecycleRegistry.handleLifecycleEvent(event)

    fun performRestore(savedState: android.os.Bundle?) =
      savedStateRegistryController.performRestore(savedState)
  }
}
