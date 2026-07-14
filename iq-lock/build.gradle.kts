// build.gradle.kts (Project-level)
// Declares the Gradle plugins used across all modules. These are applied at the module level.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.hilt.android) apply false
}
