package com.puzzroom.quickstopwatch.service

import android.app.Application
import android.content.Context
import android.provider.Settings
import android.view.WindowManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.puzzroom.quickstopwatch.data.OverlayPositionRepository
import com.puzzroom.quickstopwatch.domain.FakeTimeSource
import com.puzzroom.quickstopwatch.domain.StopwatchEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSettings

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class ServiceAndControllerTest {

  private lateinit var context: Context
  private lateinit var windowManager: WindowManager
  private lateinit var timeSource: FakeTimeSource
  private lateinit var engine: StopwatchEngine
  private lateinit var controller: StopwatchOverlayController

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    timeSource = FakeTimeSource(0L)
    val testDispatcher = StandardTestDispatcher()
    engine = StopwatchEngine(timeSource, TestScope(testDispatcher), testDispatcher)
    controller =
      StopwatchOverlayController(
        context = context,
        windowManager = windowManager,
        engine = engine,
        positionRepository = OverlayPositionRepository(context),
      )
  }

  @Test
  fun `TC-015 FLAG_KEEP_SCREEN_ON is set in window layout params`() {
    controller.show()
    assertTrue(controller.isShowing)

    val params = controller.getLayoutParams()
    assertNotNull(params)
    val hasKeepScreenOn = (params!!.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0
    assertTrue(hasKeepScreenOn)

    controller.hide()
    assertFalse(controller.isShowing)
  }

  @Test
  fun `TC-022 process cleanup or hide resets controller state`() {
    controller.show()
    engine.toggle()
    assertTrue(controller.isShowing)

    controller.hide()
    assertFalse(controller.isShowing)
    assertEquals("00:00", engine.uiState.value.text)
  }

  @Test
  fun `TC-029 show executes quickly under 500ms`() {
    val start = System.currentTimeMillis()
    controller.show()
    val duration = System.currentTimeMillis() - start
    assertTrue("Show took too long: ${duration}ms", duration < 500)
    controller.hide()
  }

  @Test
  fun `TC-002 tile click starts service when permission granted`() {
    ShadowSettings.setCanDrawOverlays(true)
    val tileService = Robolectric.buildService(QuickStopWatchTileService::class.java).create().get()

    tileService.onClick()

    val app = ApplicationProvider.getApplicationContext<Application>()
    val nextIntent = shadowOf(app).nextStartedService
    assertNotNull(nextIntent)
    assertEquals(StopwatchOverlayService.ACTION_TOGGLE, nextIntent.action)
  }

  @Test
  fun `TC-003 tile click opens settings when permission denied`() {
    ShadowSettings.setCanDrawOverlays(false)
    val tileService = Robolectric.buildService(QuickStopWatchTileService::class.java).create().get()

    tileService.onClick()

    val app = ApplicationProvider.getApplicationContext<Application>()
    val nextActivity = shadowOf(app).nextStartedActivity
    assertNotNull(nextActivity)
    assertEquals(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, nextActivity.action)
  }
}
