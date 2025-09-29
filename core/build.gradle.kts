plugins {
	id("com.android.library")
	kotlin("android")
	kotlin("kapt")
}

android {
	namespace = "com.example.core"
	compileSdk = 34
	defaultConfig { minSdk = 24 }
	buildFeatures { compose = true }
	composeOptions { kotlinCompilerExtensionVersion = "1.5.15" }
}

dependencies {
	implementation(platform(libs.compose.bom))
	implementation(libs.bundles.compose_base)
	implementation(libs.coroutines)
}
