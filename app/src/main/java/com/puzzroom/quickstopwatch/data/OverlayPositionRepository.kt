package com.puzzroom.quickstopwatch.data

import android.content.Context
import android.content.SharedPreferences

data class Position(val x: Int, val y: Int)

/** Repository to persist and restore floating stopwatch coordinates (DD-004). */
class OverlayPositionRepository(
  private val context: Context,
  private val sharedPreferences: SharedPreferences =
    context.getSharedPreferences("quickstopwatch_prefs", Context.MODE_PRIVATE),
) {
  fun loadPosition(screenWidth: Int, screenHeight: Int, sizePx: Int): Position {
    val defaultX = (screenWidth - sizePx - 16).coerceAtLeast(0)
    val defaultY = (screenHeight / 2 - sizePx / 2).coerceAtLeast(0)

    val savedX = sharedPreferences.getInt(KEY_X, defaultX)
    val savedY = sharedPreferences.getInt(KEY_Y, defaultY)

    // Clamp within screen boundaries (REQ-012)
    val clampedX = savedX.coerceIn(0, (screenWidth - sizePx).coerceAtLeast(0))
    val clampedY = savedY.coerceIn(0, (screenHeight - sizePx).coerceAtLeast(0))
    return Position(clampedX, clampedY)
  }

  fun savePosition(position: Position) {
    sharedPreferences.edit().putInt(KEY_X, position.x).putInt(KEY_Y, position.y).apply()
  }

  companion object {
    private const val KEY_X = "overlay_pos_x"
    private const val KEY_Y = "overlay_pos_y"
  }
}
