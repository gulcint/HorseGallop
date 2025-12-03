plugins {
	id("com.android.library")
	kotlin("android")
	kotlin("kapt")
	id("com.google.dagger.hilt.android")
}

android {
	namespace = "com.horsegallop.data"
	compileSdk = 34
	defaultConfig { minSdk = 24 }
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	kotlinOptions {
		jvmTarget = "17"
	}
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(libs.coroutines)
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi.kotlin)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    kapt(libs.room.compiler)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.google.auth)
}
