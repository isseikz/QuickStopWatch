import com.github.triplet.gradle.androidpublisher.ReleaseStatus
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.play.publisher)
}

// ---------------------------------------------------------------------------
// Versioning: CI passes -PappVersionName=X.Y.Z (derived from the vX.Y.Z tag).
// versionCode = X * 1_000_000 + Y * 1_000 + Z (monotonic for semver tags).
// ---------------------------------------------------------------------------
val appVersionName: String = providers.gradleProperty("appVersionName").getOrElse("0.0.1")
val appVersionCode: Int =
  appVersionName
    .split(".")
    .map { it.takeWhile(Char::isDigit).ifEmpty { "0" }.toInt() }
    .let { parts ->
      require(parts.size == 3 && parts[1] < 1000 && parts[2] < 1000) {
        "appVersionName must be X.Y.Z (Y,Z < 1000), was '$appVersionName'"
      }
      parts[0] * 1_000_000 + parts[1] * 1_000 + parts[2]
    }

// ---------------------------------------------------------------------------
// Release signing: environment variables (CI) take precedence over
// keystore.properties (local, git-ignored). If neither is present the release
// build is left unsigned so that debug/CI checks still work.
// ---------------------------------------------------------------------------
val keystoreProperties =
  Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
  }

fun signingValue(env: String, key: String): String? =
  providers.environmentVariable(env).orNull ?: keystoreProperties.getProperty(key)

val releaseStoreFile = signingValue("RELEASE_KEYSTORE_PATH", "storeFile")

android {
  namespace = "com.puzzroom.quickstopwatch"
  compileSdk = 36
  defaultConfig {
    applicationId = "com.puzzroom.quickstopwatch"
    minSdk = 31
    targetSdk = 36
    versionCode = appVersionCode
    versionName = appVersionName
  }

  signingConfigs {
    if (releaseStoreFile != null) {
      create("release") {
        storeFile = file(releaseStoreFile)
        storePassword = signingValue("RELEASE_KEYSTORE_PASSWORD", "storePassword")
        keyAlias = signingValue("RELEASE_KEY_ALIAS", "keyAlias")
        keyPassword = signingValue("RELEASE_KEY_PASSWORD", "keyPassword")
      }
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.findByName("release")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  buildFeatures {
    compose = true
    aidl = false
    buildConfig = false
    shaders = false
  }

  packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }

  // Harness: Robolectric + Roborazzi run Compose UI/screenshot tests on the JVM.
  testOptions {
    unitTests {
      isIncludeAndroidResources = true
      all { it.systemProperties["robolectric.pixelCopyRenderMode"] = "hardware" }
    }
  }

  // Harness: lint is a gate. warningsAsErrors is enabled at phase 6 kick-off.
  lint {
    abortOnError = true
    warningsAsErrors = false
    checkReleaseBuilds = true
    htmlReport = true
    xmlReport = true
  }
}

kotlin { jvmToolchain(17) }

// Harness (L2 UI loop): every @Preview is rendered to src/test/screenshots by
// `recordRoborazziDebug` and compared by `verifyRoborazziDebug`.
roborazzi {
  outputDir.set(file("src/test/screenshots"))
  generateComposePreviewRobolectricTests {
    enable = true
    packages = listOf("com.puzzroom.quickstopwatch")
    includePrivatePreviews = true
  }
}

// Harness (L5 release loop): Gradle Play Publisher.
// Credentials come from the ANDROID_PUBLISHER_CREDENTIALS env var (JSON content).
// -PplayReleaseStatus=draft is required while the Play app is still a draft.
play {
  track.set("internal")
  defaultToAppBundles.set(true)
  releaseStatus.set(
    ReleaseStatus.valueOf(
      providers.gradleProperty("playReleaseStatus").getOrElse("completed").uppercase(),
    ),
  )
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)
  testImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.turbine)
  // Local tests: Compose UI on the JVM (Robolectric) + screenshots (Roborazzi)
  testImplementation(libs.androidx.test.ext.junit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  testImplementation(libs.roborazzi.compose.preview.scanner.support)
  testImplementation(libs.composable.preview.scanner)

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)
}
