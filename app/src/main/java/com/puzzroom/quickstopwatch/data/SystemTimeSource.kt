package com.puzzroom.quickstopwatch.data

import android.os.SystemClock
import com.puzzroom.quickstopwatch.domain.TimeSource

/** Production implementation of [TimeSource] using [SystemClock.elapsedRealtime] (DD-002). */
class SystemTimeSource : TimeSource {
  override fun elapsedRealtimeMillis(): Long = SystemClock.elapsedRealtime()
}
