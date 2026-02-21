plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
    alias(libs.plugins.hilt.android)
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
			buildConfigField("String", "BASE_URL", "\"https://api-5cmhzb3wtq-ew.a.run.app/\"")
		}
		getByName("release") {
			buildConfigField("String", "BASE_URL", "\"https://api-5cmhzb3wtq-ew.a.run.app/\"")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
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
