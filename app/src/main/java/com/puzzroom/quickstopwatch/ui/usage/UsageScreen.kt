package com.puzzroom.quickstopwatch.ui.usage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.puzzroom.quickstopwatch.R
import com.puzzroom.quickstopwatch.theme.QuickStopWatchTheme

object UsageScreenTestTags {
  const val ROOT = "usage_screen"
  const val STEP_PREFIX = "usage_step_"
}

private data class UsageStep(val titleRes: Int, val bodyRes: Int)

private val UsageSteps =
  listOf(
    UsageStep(R.string.usage_step_tile_title, R.string.usage_step_tile_body),
    UsageStep(R.string.usage_step_permission_title, R.string.usage_step_permission_body),
    UsageStep(R.string.usage_step_toggle_title, R.string.usage_step_toggle_body),
    UsageStep(R.string.usage_step_operate_title, R.string.usage_step_operate_body),
  )

/** How-to-use screen shown when the launcher icon is tapped (SCR-001). Explanation only. */
@Composable
fun UsageScreen(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.testTag(UsageScreenTestTags.ROOT).verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
      text = stringResource(R.string.usage_title),
      style = MaterialTheme.typography.headlineMedium,
    )
    Text(
      text = stringResource(R.string.usage_summary),
      style = MaterialTheme.typography.bodyLarge,
    )
    UsageSteps.forEachIndexed { index, step ->
      UsageStepCard(
        number = index + 1,
        title = stringResource(step.titleRes),
        body = stringResource(step.bodyRes),
        modifier = Modifier.testTag("${UsageScreenTestTags.STEP_PREFIX}${index + 1}"),
      )
    }
    Text(
      text = stringResource(R.string.usage_note_lock_screen),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun UsageStepCard(number: Int, title: String, body: String, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    color = MaterialTheme.colorScheme.surfaceContainer,
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.Top,
    ) {
      Surface(
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
      ) {
        Text(
          text = number.toString(),
          modifier = Modifier.padding(top = 4.dp),
          style = MaterialTheme.typography.titleMedium,
          textAlign = TextAlign.Center,
        )
      }
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = body, style = MaterialTheme.typography.bodyMedium)
      }
    }
  }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun UsageScreenPreview() {
  QuickStopWatchTheme(dynamicColor = false) { UsageScreen(Modifier.padding(16.dp)) }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, locale = "ja")
@Composable
private fun UsageScreenJapanesePreview() {
  QuickStopWatchTheme(dynamicColor = false) { UsageScreen(Modifier.padding(16.dp)) }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1400, fontScale = 1.6f)
@Composable
private fun UsageScreenLargeFontPreview() {
  QuickStopWatchTheme(dynamicColor = false) { UsageScreen(Modifier.padding(16.dp)) }
}
