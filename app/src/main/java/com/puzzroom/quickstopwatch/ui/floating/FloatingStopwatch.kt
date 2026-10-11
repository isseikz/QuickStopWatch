package com.puzzroom.quickstopwatch.ui.floating

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.puzzroom.quickstopwatch.R
import com.puzzroom.quickstopwatch.theme.QuickStopWatchTheme

/** Visual / behavioural mode of the floating stopwatch (SCR-002). */
enum class StopwatchMode {
  /** Shown but never started (`00:00`). */
  Idle,
  Running,
  Paused,
  /** Reached `999:59`; looks like [Paused] and ignores taps. */
  Limit,
}

/** Everything the stateless [FloatingStopwatch] needs to render (SCR-002). */
data class StopwatchUiState(val text: String, val mode: StopwatchMode)

object FloatingStopwatchTestTags {
  const val ROOT = "floating_stopwatch"
  const val TIME = "floating_stopwatch_time"
}

/** Diameter of the floating stopwatch (REQ-004). */
val FloatingStopwatchSize = 72.dp

/**
 * The circular, FAB-sized stopwatch that is drawn on top of other apps (SCR-002).
 *
 * A tap toggles start / pause ([onTap]); dragging reports pixel deltas ([onDrag]) so that the host
 * window can be moved, and [onDragEnd] when the finger is lifted.
 */
@Composable
fun FloatingStopwatch(
  state: StopwatchUiState,
  modifier: Modifier = Modifier,
  onTap: () -> Unit = {},
  onDrag: (Offset) -> Unit = {},
  onDragEnd: () -> Unit = {},
) {
  val colors = MaterialTheme.colorScheme
  val (container, content) =
    when (state.mode) {
      StopwatchMode.Idle -> colors.secondaryContainer to colors.onSecondaryContainer
      StopwatchMode.Running -> colors.primary to colors.onPrimary
      StopwatchMode.Paused,
      StopwatchMode.Limit -> colors.tertiaryContainer to colors.onTertiaryContainer
    }

  val haptic = LocalHapticFeedback.current
  val currentMode by rememberUpdatedState(state.mode)
  val currentOnTap by rememberUpdatedState(onTap)
  val currentOnDrag by rememberUpdatedState(onDrag)
  val currentOnDragEnd by rememberUpdatedState(onDragEnd)

  val stateLabel =
    stringResource(
      when (state.mode) {
        StopwatchMode.Idle -> R.string.floating_state_idle
        StopwatchMode.Running -> R.string.floating_state_running
        StopwatchMode.Paused -> R.string.floating_state_paused
        StopwatchMode.Limit -> R.string.floating_state_limit
      },
    )
  val actionLabel =
    when (state.mode) {
      StopwatchMode.Idle -> stringResource(R.string.floating_action_start)
      StopwatchMode.Running -> stringResource(R.string.floating_action_pause)
      StopwatchMode.Paused -> stringResource(R.string.floating_action_resume)
      StopwatchMode.Limit -> null
    }
  val description = stringResource(R.string.floating_description, state.text)

  // The overlay is a fixed-size FAB: size the text in dp so that it never overflows the circle
  // when the system font scale is large (TalkBack announces the time instead, NFR-003).
  val fontSize = with(LocalDensity.current) { (if (state.text.length > 5) 16.dp else 20.dp).toSp() }

  Box(
    modifier =
      modifier
        .testTag(FloatingStopwatchTestTags.ROOT)
        .size(FloatingStopwatchSize)
        .clip(CircleShape)
        .background(container)
        .border(1.dp, colors.outline.copy(alpha = 0.5f), CircleShape)
        .pointerInput(Unit) {
          detectTapGestures(
            onTap = {
              if (currentMode != StopwatchMode.Limit) {
                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
              }
              currentOnTap()
            },
          )
        }
        .pointerInput(Unit) {
          detectDragGestures(
            onDragEnd = { currentOnDragEnd() },
            onDragCancel = { currentOnDragEnd() },
            onDrag = { change, amount ->
              change.consume()
              currentOnDrag(amount)
            },
          )
        }
        .semantics(mergeDescendants = true) {
          contentDescription = description
          stateDescription = stateLabel
          if (actionLabel != null) {
            onClick(label = actionLabel) {
              currentOnTap()
              true
            }
          }
        },
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = state.text,
      modifier = Modifier.testTag(FloatingStopwatchTestTags.TIME),
      color = content,
      fontSize = fontSize,
      fontWeight = FontWeight.Medium,
      textAlign = TextAlign.Center,
      maxLines = 1,
      style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
    )
  }
}

private const val PREVIEW_BACKGROUND = 0xFF8A8A8A

@Preview(widthDp = 112, heightDp = 112, showBackground = true, backgroundColor = PREVIEW_BACKGROUND)
@Composable
private fun FloatingStopwatchIdlePreview() {
  PreviewFrame { FloatingStopwatch(StopwatchUiState("00:00", StopwatchMode.Idle)) }
}

@Preview(widthDp = 112, heightDp = 112, showBackground = true, backgroundColor = PREVIEW_BACKGROUND)
@Composable
private fun FloatingStopwatchRunningPreview() {
  PreviewFrame { FloatingStopwatch(StopwatchUiState("03:21", StopwatchMode.Running)) }
}

@Preview(widthDp = 112, heightDp = 112, showBackground = true, backgroundColor = PREVIEW_BACKGROUND)
@Composable
private fun FloatingStopwatchPausedPreview() {
  PreviewFrame { FloatingStopwatch(StopwatchUiState("12:07", StopwatchMode.Paused)) }
}

@Preview(widthDp = 112, heightDp = 112, showBackground = true, backgroundColor = PREVIEW_BACKGROUND)
@Composable
private fun FloatingStopwatchThreeDigitMinutesPreview() {
  PreviewFrame { FloatingStopwatch(StopwatchUiState("999:59", StopwatchMode.Limit)) }
}

@Preview(
  widthDp = 112,
  heightDp = 112,
  showBackground = true,
  backgroundColor = 0xFF202020,
  uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
  fontScale = 2f,
)
@Composable
private fun FloatingStopwatchDarkLargeFontPreview() {
  PreviewFrame { FloatingStopwatch(StopwatchUiState("125:07", StopwatchMode.Running)) }
}

@Composable
private fun PreviewFrame(content: @Composable () -> Unit) {
  QuickStopWatchTheme(dynamicColor = false) {
    Box(Modifier.size(112.dp).background(Color.Transparent), contentAlignment = Alignment.Center) {
      content()
    }
  }
}
