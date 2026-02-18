plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    kotlin("kapt")
}
android {
	namespace = "com.horsegallop.core"
	compileSdk = 34
	defaultConfig { minSdk = 24 }
	buildFeatures { compose = true }
    
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
    
    lint {
        abortOnError = false
        checkReleaseBuilds = false
        ignoreWarnings = true
    }
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
	implementation(platform(libs.compose.bom))
	implementation(libs.compose.ui)
	implementation(libs.compose.material3)
	implementation(libs.compose.icons.extended)
	implementation(libs.coroutines)
	implementation(libs.androidx.core.ktx)
	implementation(libs.shimmer.compose)
	implementation(libs.lottie.compose)
	
	// Material Compose Icons for bottom nav
	implementation("androidx.compose.material:material-icons-core:1.7.2")
	implementation("androidx.compose.material:material-icons-extended:1.7.2")
}
