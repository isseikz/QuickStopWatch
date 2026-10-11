package com.puzzroom.quickstopwatch.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class OverlayPositionRepositoryTest {

  private lateinit var context: Context
  private lateinit var repository: OverlayPositionRepository

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    context
      .getSharedPreferences("quickstopwatch_prefs", Context.MODE_PRIVATE)
      .edit()
      .clear()
      .commit()
    repository = OverlayPositionRepository(context)
  }

  @Test
  fun `TC-017 default position is near right edge and vertically centered`() {
    val screenWidth = 1080
    val screenHeight = 2400
    val fabSize = 200

    val pos = repository.loadPosition(screenWidth, screenHeight, fabSize)
    // Default X should be near right edge: 1080 - 200 - 16 = 864
    assertEquals(864, pos.x)
    // Default Y should be vertically centered: 2400 / 2 - 200 / 2 = 1100
    assertEquals(1100, pos.y)
  }

  @Test
  fun `TC-018 saved position is restored and clamped`() {
    val screenWidth = 1080
    val screenHeight = 2400
    val fabSize = 200

    repository.savePosition(Position(120, 340))
    val restored = repository.loadPosition(screenWidth, screenHeight, fabSize)
    assertEquals(120, restored.x)
    assertEquals(340, restored.y)

    // Clamping when saved is out of screen
    repository.savePosition(Position(2000, 3000))
    val clamped = repository.loadPosition(screenWidth, screenHeight, fabSize)
    assertEquals(1080 - 200, clamped.x)
    assertEquals(2400 - 200, clamped.y)
  }
}
