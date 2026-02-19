plugins {
	id("com.android.library")
	kotlin("android")
}

android {
	namespace = "com.example.domain"
	compileSdk = 34
	defaultConfig { minSdk = 24 }
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
}

dependencies {
	implementation(libs.coroutines)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
	kotlinOptions {
		jvmTarget = "17"
	}
}
