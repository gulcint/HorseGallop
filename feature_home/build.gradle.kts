plugins {
	id("com.android.library")
	kotlin("android")
	kotlin("kapt")
	id("app.cash.paparazzi")
}

android {
	namespace = "com.horsegallop.feature_home"
	compileSdk = 34
	defaultConfig { minSdk = 24 }
	buildFeatures { compose = true }
	composeOptions { kotlinCompilerExtensionVersion = "1.5.15" }
}

dependencies {
	implementation(libs.hilt.android)
	kapt(libs.hilt.compiler)
	implementation(project(":core"))
	implementation(project(":domain"))
	implementation(platform(libs.compose.bom))
	implementation(libs.bundles.compose.base)
	implementation(libs.compose.icons.extended)
	implementation(libs.coil.compose)
	implementation(libs.lifecycle.compose)
	implementation(libs.lottie.compose)
	implementation(libs.shimmer.compose)
	
	// Screenshot testing
	testImplementation("app.cash.paparazzi:paparazzi:1.3.4")
	testImplementation("junit:junit:4.13.2")
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
