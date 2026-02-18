plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kapt) apply false
    alias(libs.plugins.hilt.android) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}
