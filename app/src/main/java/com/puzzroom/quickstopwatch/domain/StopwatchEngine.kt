package com.puzzroom.quickstopwatch.domain

import com.puzzroom.quickstopwatch.ui.floating.StopwatchMode
import com.puzzroom.quickstopwatch.ui.floating.StopwatchUiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Domain engine managing stopwatch state transitions, time calculation and UI state (DD-003). */
class StopwatchEngine(
  private val timeSource: TimeSource,
  private val coroutineScope: CoroutineScope,
  private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
  private val _uiState =
    MutableStateFlow(StopwatchUiState(text = "00:00", mode = StopwatchMode.Idle))
  val uiState: StateFlow<StopwatchUiState> = _uiState.asStateFlow()

  private var accumulatedMillis: Long = 0L
  private var startRealtime: Long = 0L
  private var tickerJob: Job? = null

  /** Max supported display time: 999 minutes 59 seconds (REQ-007). */
  private val maxElapsedSeconds = 999 * 60 + 59
  private val maxElapsedMillis = maxElapsedSeconds * 1000L

  fun toggle() {
    when (_uiState.value.mode) {
      StopwatchMode.Idle,
      StopwatchMode.Paused -> start()
      StopwatchMode.Running -> pause()
      StopwatchMode.Limit -> {
        // Ignored as per REQ-007
      }
    }
  }

  fun reset() {
    stopTicker()
    accumulatedMillis = 0L
    startRealtime = 0L
    _uiState.value = StopwatchUiState(text = "00:00", mode = StopwatchMode.Idle)
  }

  fun stopTicker() {
    tickerJob?.cancel()
    tickerJob = null
  }

  private fun start() {
    startRealtime = timeSource.elapsedRealtimeMillis()
    _uiState.value = _uiState.value.copy(mode = StopwatchMode.Running)
    startTicker()
  }

  private fun pause() {
    stopTicker()
    accumulatedMillis = calculateElapsedMillis()
    _uiState.value =
      StopwatchUiState(text = formatTime(accumulatedMillis), mode = StopwatchMode.Paused)
  }

  private fun startTicker() {
    tickerJob?.cancel()
    tickerJob =
      coroutineScope.launch(dispatcher) {
        while (isActive) {
          val elapsed = calculateElapsedMillis()
          if (elapsed >= maxElapsedMillis) {
            _uiState.value = StopwatchUiState(text = "999:59", mode = StopwatchMode.Limit)
            tickerJob?.cancel()
            break
          } else {
            _uiState.value =
              StopwatchUiState(text = formatTime(elapsed), mode = StopwatchMode.Running)
          }
          delay(100L) // Polling interval to ensure accurate second updates
        }
      }
  }

  private fun calculateElapsedMillis(): Long {
    val currentRealtime = timeSource.elapsedRealtimeMillis()
    val sessionElapsed = (currentRealtime - startRealtime).coerceAtLeast(0L)
    return (accumulatedMillis + sessionElapsed).coerceAtMost(maxElapsedMillis)
  }

  companion object {
    fun formatTime(elapsedMillis: Long): String {
      val totalSeconds = (elapsedMillis / 1000L).coerceAtLeast(0L)
      val minutes = totalSeconds / 60
      val seconds = totalSeconds % 60
      return "%02d:%02d".format(minutes, seconds)
    }
  }
}
