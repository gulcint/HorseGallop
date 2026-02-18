plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    kotlin("kapt")
//    alias(libs.plugins.skydoves.stability.analyzer)
}

android {
	namespace = "com.horsegallop.core"
	compileSdk = 34
	defaultConfig { minSdk = 24 }
	buildFeatures { compose = true }
}

dependencies {
	implementation(platform(libs.compose.bom))
	implementation(libs.bundles.compose.base)
	implementation(libs.compose.icons.extended)
	implementation(libs.coroutines)
	implementation(libs.androidx.core.ktx)
	implementation(libs.shimmer.compose)
	implementation(libs.lottie.compose)
	implementation("androidx.compose.animation:animation:1.6.8")
	implementation("androidx.compose.foundation:foundation:1.6.8")
}

android {
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
