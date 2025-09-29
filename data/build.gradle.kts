plugins {
	id("com.android.library")
	kotlin("android")
	kotlin("kapt")
}

android {
	namespace = "com.example.data"
	compileSdk = 34
	defaultConfig { minSdk = 24 }
}

dependencies {
	implementation(project(":domain"))
	implementation(project(":core"))
	implementation(libs.coroutines)
	implementation(libs.retrofit)
	implementation(libs.retrofit.moshi)
	implementation(libs.okhttp)
	implementation(libs.okhttp.logging)
	implementation(libs.room.runtime)
	implementation(libs.room.ktx)
	kapt(libs.room.compiler)
}
