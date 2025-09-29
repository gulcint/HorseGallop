plugins {
	id("com.android.application")
	kotlin("android")
	kotlin("kapt")
	id("com.google.dagger.hilt.android")
	id("com.google.gms.google-services")
}

android {
	namespace = "com.example.adincountry"
	compileSdk = 34
	defaultConfig {
		applicationId = "com.example.adincountry"
		minSdk = 24
		targetSdk = 34
		versionCode = 1
		versionName = "0.1.0"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		vectorDrawables.useSupportLibrary = true
	}
	buildFeatures { compose = true }
	composeOptions { kotlinCompilerExtensionVersion = "1.5.15" }
	packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
	buildTypes {
		getByName("debug") { isMinifyEnabled = false }
		getByName("release") {
			isMinifyEnabled = true
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}
}

dependencies {
	implementation(platform(libs.compose.bom))
	implementation(libs.bundles.compose.base)
	implementation(libs.lifecycle.runtime)
	implementation(libs.lifecycle.vm)
	implementation(libs.lifecycle.compose)
	implementation(libs.coroutines)
	implementation(libs.hilt.android)
	kapt(libs.hilt.compiler)
	implementation(platform(libs.firebase.bom))
	implementation(libs.firebase.auth)
	implementation(libs.firebase.messaging)
	implementation(libs.google.auth)
	implementation("androidx.core:core-splashscreen:1.0.1")
	implementation(project(":core"))
	implementation(project(":domain"))
	implementation(project(":feature_auth"))
	implementation(project(":feature_home"))
	implementation(project(":feature_schedule"))
	implementation(project(":feature_reservation"))
	implementation(project(":feature_orders"))
	implementation(project(":feature_reviews"))
	implementation(project(":feature_admin"))
	debugImplementation(libs.compose.tooling)
	testImplementation("junit:junit:4.13.2")
	androidTestImplementation("androidx.test.ext:junit:1.2.1")
	androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
