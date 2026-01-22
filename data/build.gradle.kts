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
	buildFeatures {
		buildConfig = true
	}
	buildTypes {
		getByName("debug") {
			buildConfigField("String", "BASE_URL", "\"https://api.example.com/\"")
		}
		getByName("release") {
			buildConfigField("String", "BASE_URL", "\"https://api.example.com/\"")
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
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(libs.coroutines)
    implementation(libs.google.location)
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi.kotlin)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.google.auth)
}
