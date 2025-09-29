plugins {
	id("com.android.library")
	kotlin("android")
	kotlin("kapt")
	id("com.google.dagger.hilt.android")
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
	implementation(libs.hilt.android)
	kapt(libs.hilt.compiler)
	kapt(libs.room.compiler)
}

android {
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
	kotlinOptions {
		jvmTarget = "17"
	}
}
