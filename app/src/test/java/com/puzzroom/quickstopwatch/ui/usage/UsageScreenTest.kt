package com.puzzroom.quickstopwatch.ui.usage

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class UsageScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `TC-023 usage instructions are displayed in 4 steps`() {
    composeTestRule.setContent { UsageScreen() }

    composeTestRule.onNodeWithTag(UsageScreenTestTags.ROOT).assertIsDisplayed()
    composeTestRule
      .onNodeWithTag("${UsageScreenTestTags.STEP_PREFIX}1", useUnmergedTree = true)
      .assertExists()
    composeTestRule
      .onNodeWithTag("${UsageScreenTestTags.STEP_PREFIX}2", useUnmergedTree = true)
      .assertExists()
    composeTestRule
      .onNodeWithTag("${UsageScreenTestTags.STEP_PREFIX}3", useUnmergedTree = true)
      .assertExists()
    composeTestRule
      .onNodeWithTag("${UsageScreenTestTags.STEP_PREFIX}4", useUnmergedTree = true)
      .assertExists()
  }
}
