package com.puzzroom.quickstopwatch.domain

class FakeTimeSource(var currentMillis: Long = 0L) : TimeSource {
  override fun elapsedRealtimeMillis(): Long = currentMillis

  fun advanceTimeBy(millis: Long) {
    currentMillis += millis
  }
}
