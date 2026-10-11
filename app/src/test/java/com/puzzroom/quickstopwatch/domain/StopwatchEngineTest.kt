package com.puzzroom.quickstopwatch.domain

import com.puzzroom.quickstopwatch.ui.floating.StopwatchMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StopwatchEngineTest {

  @Test
  fun `TC-005 toggle from Idle starts running`() = runTest {
    val timeSource = FakeTimeSource(1000L)
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val engine = StopwatchEngine(timeSource, this, testDispatcher)

    assertEquals(StopwatchMode.Idle, engine.uiState.value.mode)
    assertEquals("00:00", engine.uiState.value.text)

    engine.toggle()
    testScheduler.advanceTimeBy(100L)

    assertEquals(StopwatchMode.Running, engine.uiState.value.mode)
    engine.stopTicker()
  }

  @Test
  fun `TC-006 toggle from Running pauses time`() = runTest {
    val timeSource = FakeTimeSource(1000L)
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val engine = StopwatchEngine(timeSource, this, testDispatcher)

    engine.toggle() // Start
    timeSource.advanceTimeBy(3000L)
    testScheduler.advanceTimeBy(3100L)

    assertEquals("00:03", engine.uiState.value.text)
    assertEquals(StopwatchMode.Running, engine.uiState.value.mode)

    engine.toggle() // Pause
    assertEquals(StopwatchMode.Paused, engine.uiState.value.mode)
    assertEquals("00:03", engine.uiState.value.text)

    // Time advances while paused -> display remains unchanged
    timeSource.advanceTimeBy(5000L)
    testScheduler.advanceTimeBy(5000L)
    assertEquals("00:03", engine.uiState.value.text)
    engine.stopTicker()
  }

  @Test
  fun `TC-007 toggle from Paused resumes counting`() = runTest {
    val timeSource = FakeTimeSource(1000L)
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val engine = StopwatchEngine(timeSource, this, testDispatcher)

    engine.toggle() // Start
    timeSource.advanceTimeBy(2000L)
    testScheduler.advanceTimeBy(2100L)
    engine.toggle() // Pause at 2s

    timeSource.advanceTimeBy(10000L) // Paused interval
    engine.toggle() // Resume

    timeSource.advanceTimeBy(3000L)
    testScheduler.advanceTimeBy(3100L)

    assertEquals(StopwatchMode.Running, engine.uiState.value.mode)
    assertEquals("00:05", engine.uiState.value.text)
    engine.stopTicker()
  }

  @Test
  fun `TC-008 format 59m 59s to 60m 00s`() {
    val fiftyNine59 = (59 * 60 + 59) * 1000L
    assertEquals("59:59", StopwatchEngine.formatTime(fiftyNine59))

    val sixtyMinutes = 60 * 60 * 1000L
    assertEquals("60:00", StopwatchEngine.formatTime(sixtyMinutes))
  }

  @Test
  fun `TC-009 format 125m 07s`() {
    val time = (125 * 60 + 7) * 1000L
    assertEquals("125:07", StopwatchEngine.formatTime(time))
  }

  @Test
  fun `TC-010 format 9m 5s with leading zero`() {
    val time = (9 * 60 + 5) * 1000L
    assertEquals("09:05", StopwatchEngine.formatTime(time))
  }

  @Test
  fun `TC-011 limit reached at 999m 59s and ignores toggle`() = runTest {
    val timeSource = FakeTimeSource(0L)
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val engine = StopwatchEngine(timeSource, this, testDispatcher)

    engine.toggle() // Start
    val maxMillis = (999 * 60 + 59) * 1000L
    timeSource.advanceTimeBy(maxMillis + 5000L)
    testScheduler.advanceTimeBy(maxMillis + 5100L)

    assertEquals(StopwatchMode.Limit, engine.uiState.value.mode)
    assertEquals("999:59", engine.uiState.value.text)

    // Toggle should be ignored
    engine.toggle()
    assertEquals(StopwatchMode.Limit, engine.uiState.value.mode)
    assertEquals("999:59", engine.uiState.value.text)
    engine.stopTicker()
  }

  @Test
  fun `TC-012 fractional seconds are truncated`() {
    assertEquals("00:01", StopwatchEngine.formatTime(1999L))
    assertEquals("00:00", StopwatchEngine.formatTime(999L))
  }

  @Test
  fun `TC-013 sleep duration is preserved across elapsedRealtime`() = runTest {
    val timeSource = FakeTimeSource(10_000L)
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val engine = StopwatchEngine(timeSource, this, testDispatcher)

    engine.toggle()
    // Simulated deep sleep where device sleeps for 15 seconds
    timeSource.advanceTimeBy(15_000L)
    testScheduler.advanceTimeBy(15_100L)

    assertEquals("00:15", engine.uiState.value.text)
    engine.stopTicker()
  }

  @Test
  fun `TC-014 reset resets state to Idle 00-00`() = runTest {
    val timeSource = FakeTimeSource(1000L)
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val engine = StopwatchEngine(timeSource, this, testDispatcher)

    engine.toggle()
    timeSource.advanceTimeBy(5000L)
    testScheduler.advanceTimeBy(5100L)
    assertEquals("00:05", engine.uiState.value.text)

    engine.reset()
    assertEquals(StopwatchMode.Idle, engine.uiState.value.mode)
    assertEquals("00:00", engine.uiState.value.text)
    engine.stopTicker()
  }

  @Test
  fun `TC-021 lock screen continuation counting`() = runTest {
    val timeSource = FakeTimeSource(0L)
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val engine = StopwatchEngine(timeSource, this, testDispatcher)

    engine.toggle()
    timeSource.advanceTimeBy(30_000L)
    testScheduler.advanceTimeBy(30_100L)

    assertEquals("00:30", engine.uiState.value.text)
    engine.stopTicker()
  }

  @Test
  fun `TC-028 accuracy verified against real time delta`() = runTest {
    val timeSource = FakeTimeSource(0L)
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val engine = StopwatchEngine(timeSource, this, testDispatcher)

    engine.toggle()
    for (sec in 1..100) {
      timeSource.advanceTimeBy(1000L)
      testScheduler.advanceTimeBy(1000L)
      val expectedMinutes = sec / 60
      val expectedSeconds = sec % 60
      val expectedStr = "%02d:%02d".format(expectedMinutes, expectedSeconds)
      assertEquals(expectedStr, engine.uiState.value.text)
    }
    engine.stopTicker()
  }

  @Test
  fun `TC-030 subsecond delay does not produce extra formatted time changes`() {
    assertEquals("00:05", StopwatchEngine.formatTime(5100L))
    assertEquals("00:05", StopwatchEngine.formatTime(5900L))
  }
}
