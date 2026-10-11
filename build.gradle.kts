// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.play.publisher) apply false
  alias(libs.plugins.spotless)
}

// Harness: single formatting authority for agents and CI (`./gradlew spotlessApply`).
spotless {
  kotlin {
    target("**/*.kt")
    targetExclude("**/build/**")
    ktfmt().googleStyle()
  }
  kotlinGradle {
    target("**/*.gradle.kts")
    targetExclude("**/build/**")
    ktfmt().googleStyle()
  }
}
