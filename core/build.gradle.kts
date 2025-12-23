plugins {
	id("com.android.library")
	kotlin("android")
	kotlin("kapt")
}

android {
	namespace = "com.horsegallop.core"
	compileSdk = 34
	defaultConfig { minSdk = 24 }
	buildFeatures { compose = true }
	composeOptions { kotlinCompilerExtensionVersion = "1.5.15" }
}

dependencies {
	implementation(platform(libs.compose.bom))
	implementation(libs.bundles.compose.base)
	implementation(libs.compose.icons.extended)
	implementation(libs.coroutines)
	implementation(libs.androidx.core.ktx)
	implementation(libs.shimmer.compose)
	implementation("androidx.compose.animation:animation:1.6.8")
	implementation("androidx.compose.foundation:foundation:1.6.8")
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
