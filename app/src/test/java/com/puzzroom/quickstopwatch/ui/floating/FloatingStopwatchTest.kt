package com.puzzroom.quickstopwatch.ui.floating

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class FloatingStopwatchTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `TC-001 circular stopwatch displays over app`() {
    composeTestRule.setContent {
      FloatingStopwatch(state = StopwatchUiState(text = "00:00", mode = StopwatchMode.Idle))
    }

    composeTestRule.onNodeWithTag(FloatingStopwatchTestTags.ROOT).assertIsDisplayed()
    composeTestRule.onNodeWithText("00:00").assertIsDisplayed()
  }

  @Test
  fun `TC-004 circular fab is 72dp diameter`() {
    composeTestRule.setContent {
      FloatingStopwatch(state = StopwatchUiState(text = "00:00", mode = StopwatchMode.Idle))
    }

    composeTestRule
      .onNodeWithTag(FloatingStopwatchTestTags.ROOT)
      .assertWidthIsEqualTo(72.dp)
      .assertHeightIsEqualTo(72.dp)
  }

  @Test
  fun `TC-016 drag gesture triggers onDrag callback`() {
    var dragged = false
    composeTestRule.setContent {
      FloatingStopwatch(
        state = StopwatchUiState(text = "00:00", mode = StopwatchMode.Idle),
        onDrag = { dragged = true },
      )
    }

    composeTestRule.onNodeWithTag(FloatingStopwatchTestTags.ROOT).performTouchInput {
      swipeRight()
    }

    assertTrue(dragged)
  }

  @Test
  fun `TC-019 state visual mode and semantics representation`() {
    composeTestRule.setContent {
      FloatingStopwatch(state = StopwatchUiState(text = "03:15", mode = StopwatchMode.Running))
    }

    composeTestRule
      .onNodeWithTag(FloatingStopwatchTestTags.ROOT)
      .assertContentDescriptionEquals("Stopwatch 03:15")
  }

  @Test
  fun `TC-020 tap performs click callback and triggers haptic`() {
    var tapped = false
    composeTestRule.setContent {
      FloatingStopwatch(
        state = StopwatchUiState(text = "00:00", mode = StopwatchMode.Idle),
        onTap = { tapped = true },
      )
    }

    composeTestRule.onNodeWithTag(FloatingStopwatchTestTags.ROOT).performClick()
    assertTrue(tapped)
  }

  @Test
  fun `TC-027 accessibility semantics for TalkBack`() {
    composeTestRule.setContent {
      FloatingStopwatch(state = StopwatchUiState(text = "05:00", mode = StopwatchMode.Paused))
    }

    val node = composeTestRule.onNodeWithTag(FloatingStopwatchTestTags.ROOT)
    node.assertContentDescriptionEquals("Stopwatch 05:00")
  }
}
