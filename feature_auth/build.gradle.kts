plugins {
	id("com.android.library")
	kotlin("android")
	kotlin("kapt")
}

android {
	namespace = "com.example.feature_auth"
	compileSdk = 34
	defaultConfig { minSdk = 24 }
	buildFeatures { compose = true }
	composeOptions { kotlinCompilerExtensionVersion = "1.5.15" }
}

dependencies {
	implementation(project(":core"))
	implementation(project(":domain"))
	implementation(platform(libs.compose.bom))
	implementation(libs.bundles.compose_base)
}
