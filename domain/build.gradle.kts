plugins {
	id("com.android.library")
	kotlin("android")
}

android {
	namespace = "com.horsegallop.domain"
	compileSdk = 34
	defaultConfig { minSdk = 24 }
}

dependencies {
    implementation(libs.coroutines)
    implementation("javax.inject:javax.inject:1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
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
