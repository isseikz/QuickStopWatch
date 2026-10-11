package com.puzzroom.quickstopwatch.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.puzzroom.quickstopwatch.data.OverlayPositionRepository
import com.puzzroom.quickstopwatch.data.SystemTimeSource
import com.puzzroom.quickstopwatch.domain.StopwatchEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/** Service keeping the stopwatch overlay alive without FGS (DD-008). */
class StopwatchOverlayService : Service() {

  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private lateinit var controller: StopwatchOverlayController

  override fun onCreate() {
    super.onCreate()
    val engine =
      StopwatchEngine(
        timeSource = SystemTimeSource(),
        coroutineScope = serviceScope,
      )
    controller =
      StopwatchOverlayController(
        context = this,
        engine = engine,
        positionRepository = OverlayPositionRepository(this),
      )
    instance = this
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_SHOW -> controller.show()
      ACTION_HIDE -> {
        controller.hide()
        stopSelf()
      }
      ACTION_TOGGLE -> {
        if (controller.isShowing) {
          controller.hide()
          stopSelf()
        } else {
          controller.show()
        }
      }
    }
    return START_NOT_STICKY
  }

  override fun onDestroy() {
    controller.hide()
    serviceScope.cancel()
    instance = null
    super.onDestroy()
  }

  override fun onBind(intent: Intent?): IBinder? = null

  companion object {
    const val ACTION_SHOW = "com.puzzroom.quickstopwatch.action.SHOW"
    const val ACTION_HIDE = "com.puzzroom.quickstopwatch.action.HIDE"
    const val ACTION_TOGGLE = "com.puzzroom.quickstopwatch.action.TOGGLE"

    @Volatile
    var instance: StopwatchOverlayService? = null
      private set

    val isShowing: Boolean
      get() = instance?.controller?.isShowing == true
  }
}
