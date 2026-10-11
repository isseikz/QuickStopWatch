package com.puzzroom.quickstopwatch.domain

/**
 * Abstraction for system elapsed time since boot in milliseconds (DD-001).
 *
 * Used to measure stopwatch intervals independently of wall-clock changes or sleep.
 */
interface TimeSource {
  fun elapsedRealtimeMillis(): Long
}
