plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.horsegallop"
	compileSdk = 34
	defaultConfig {
        applicationId = "com.horsegallop"
		minSdk = 24
		targetSdk = 34
		versionCode = 1
		versionName = "0.1.0"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		vectorDrawables.useSupportLibrary = true
	}
	buildFeatures {
		compose = true
		buildConfig = true
	}
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
	implementation(libs.compose.ui)
	implementation(libs.compose.material3)
	implementation(libs.compose.tooling)
	implementation(libs.compose.navigation)
    implementation(libs.compose.icons.extended)
	implementation("androidx.appcompat:appcompat:1.7.0")
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
    implementation(libs.firebase.firestore)
    implementation("com.google.firebase:firebase-storage-ktx")
	implementation(libs.google.auth)
	implementation("androidx.core:core-splashscreen:1.0.1")
	implementation(libs.lottie.compose)
    implementation(libs.coil.compose)
    implementation(libs.shimmer.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi.kotlin)
    implementation(project(":core"))
    implementation(project(":compose"))
    implementation(project(":data"))
    implementation(project(":domain"))
	debugImplementation(libs.compose.tooling)
	testImplementation("junit:junit:4.13.2")
	androidTestImplementation("androidx.test.ext:junit:1.2.1")
	androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
	androidTestImplementation(libs.compose.ui.test.junit4)
	debugImplementation(libs.compose.ui.test.manifest)
}
