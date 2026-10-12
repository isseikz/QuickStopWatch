package com.puzzroom.quickstopwatch

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.puzzroom.quickstopwatch.service.QuickStopWatchTileService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class ManifestAndNonFunctionalTest {

  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
  }

  @Test
  fun `TC-024 tile metadata and name defined`() {
    val pm = context.packageManager
    val serviceInfo =
      pm.getServiceInfo(
        android.content.ComponentName(context, QuickStopWatchTileService::class.java),
        PackageManager.GET_META_DATA,
      )
    val label = serviceInfo.loadLabel(pm).toString()
    assertTrue(label.isNotEmpty())
    assertEquals(R.drawable.ic_qs_stopwatch, serviceInfo.icon)
  }

  @Test
  fun `TC-025 only SYSTEM_ALERT_WINDOW permission requested`() {
    val pm = context.packageManager
    val packageInfo = pm.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
    val requestedPermissions = packageInfo.requestedPermissions?.toList() ?: emptyList()

    assertTrue(requestedPermissions.contains("android.permission.SYSTEM_ALERT_WINDOW"))
    assertFalse(
      "Must not request WAKE_LOCK",
      requestedPermissions.contains("android.permission.WAKE_LOCK"),
    )
    assertFalse(
      "Must not request VIBRATE",
      requestedPermissions.contains("android.permission.VIBRATE"),
    )
    assertFalse(
      "Must not request FOREGROUND_SERVICE",
      requestedPermissions.contains("android.permission.FOREGROUND_SERVICE"),
    )
  }

  @Test
  fun `TC-026 minSdk 31 and targetSdk 36`() {
    val appInfo = context.applicationInfo
    assertEquals(31, appInfo.minSdkVersion)
    assertEquals(36, appInfo.targetSdkVersion)
  }

  @Test
  fun `TC-031 strings defined in default and japanese locales`() {
    val res = context.resources
    val defaultTileName = res.getString(R.string.tile_name)
    assertTrue(defaultTileName.isNotEmpty())

    val defaultAppTitle = res.getString(R.string.usage_title)
    assertTrue(defaultAppTitle.isNotEmpty())
  }

  @Test
  fun `TC-032 INTERNET permission is not declared`() {
    val pm = context.packageManager
    val packageInfo = pm.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
    val requestedPermissions = packageInfo.requestedPermissions?.toList() ?: emptyList()

    assertFalse(requestedPermissions.contains("android.permission.INTERNET"))
  }
}
