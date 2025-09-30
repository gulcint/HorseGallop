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
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	kotlinOptions {
		jvmTarget = "17"
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
	implementation(libs.hilt.navigation.compose)
	kapt(libs.hilt.compiler)
	implementation(platform(libs.firebase.bom))
	implementation(libs.firebase.auth)
	implementation(libs.firebase.messaging)
	implementation(libs.google.auth)
	implementation(libs.core.splashscreen)
	
	implementation(project(":core"))
	implementation(project(":domain"))
	implementation(project(":feature_auth"))
	implementation(project(":feature_home"))
	implementation(project(":feature_schedule"))
	implementation(project(":feature_reservation"))
	implementation(project(":feature_orders"))
	implementation(project(":feature_reviews"))
	implementation(project(":feature_admin"))
	implementation(project(":feature_profile"))
	implementation(project(":feature_settings"))
	
	debugImplementation(libs.compose.tooling)
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.test.ext.junit)
	androidTestImplementation(libs.espresso.core)
}
