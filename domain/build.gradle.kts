plugins {
	id("com.android.library")
	kotlin("android")
}

android {
	namespace = "com.example.domain"
	compileSdk = 34
	defaultConfig { minSdk = 24 }
}

dependencies {
	implementation(libs.coroutines)
}
