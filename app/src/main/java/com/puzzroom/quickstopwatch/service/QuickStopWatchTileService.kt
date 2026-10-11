package com.puzzroom.quickstopwatch.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/** Quick Settings Tile to show/hide the stopwatch overlay (DD-007). */
class QuickStopWatchTileService : TileService() {

  override fun onStartListening() {
    super.onStartListening()
    updateTileState()
  }

  override fun onClick() {
    super.onClick()
    if (!Settings.canDrawOverlays(this)) {
      // REQ-003: Guide user to permission settings and collapse quick settings
      openOverlaySettings()
      return
    }

    // Toggle overlay
    val intent =
      Intent(this, StopwatchOverlayService::class.java).apply {
        action = StopwatchOverlayService.ACTION_TOGGLE
      }
    startService(intent)

    // Invert tile state immediately for UI feedback
    val tile = qsTile ?: return
    tile.state = if (tile.state == Tile.STATE_ACTIVE) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
    tile.updateTile()
  }

  @SuppressLint("StartActivityAndCollapseDeprecated")
  private fun openOverlaySettings() {
    val intent =
      Intent(
          Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
          Uri.parse("package:$packageName"),
        )
        .apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

    val pendingIntent =
      PendingIntent.getActivity(
        this,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
      )
    startActivity(intent)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      try {
        startActivityAndCollapse(pendingIntent)
      } catch (_: Exception) {}
    } else {
      try {
        @Suppress("DEPRECATION") startActivityAndCollapse(intent)
      } catch (_: Exception) {}
    }

    val tile = qsTile ?: return
    tile.state = Tile.STATE_INACTIVE
    tile.updateTile()
  }

  private fun updateTileState() {
    val tile = qsTile ?: return
    if (!Settings.canDrawOverlays(this)) {
      tile.state = Tile.STATE_INACTIVE
    } else {
      tile.state = if (StopwatchOverlayService.isShowing) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
    }
    tile.updateTile()
  }
}
