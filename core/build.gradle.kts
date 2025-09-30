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
	implementation(libs.bundles.compose.base)
	implementation(libs.coroutines)
	implementation(libs.androidx.core.ktx)
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
