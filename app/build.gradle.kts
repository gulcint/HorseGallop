import java.util.Properties
import org.gradle.api.GradleException

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    kotlin("kapt")
    alias(libs.plugins.hilt.android)
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
        
        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }
        
        // Önce local.properties'den, yoksa ortam değişkenlerinden (CI/CD için) okumayı dene
        val googleMapsApiKey = properties.getProperty("GOOGLE_MAPS_API_KEY") 
            ?: System.getenv("GOOGLE_MAPS_API_KEY")
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = googleMapsApiKey
        buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"$googleMapsApiKey\"")
	}
	buildFeatures {
		compose = true
		buildConfig = true
	}
	packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
	buildTypes {
		getByName("debug") {
            isMinifyEnabled = false
            buildConfigField("String", "BASE_URL", "\"https://api-5cmhzb3wtq-ew.a.run.app/\"")
        }
		getByName("release") {
			isMinifyEnabled = true
            buildConfigField("String", "BASE_URL", "\"https://api-5cmhzb3wtq-ew.a.run.app/\"")
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}
	compileOptions {
		isCoreLibraryDesugaringEnabled = true
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

    lint {
        baseline = file("lint-baseline.xml")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

val disallowedSurfacePatterns = listOf(
    Regex("""CardDefaults\.cardColors\(\s*containerColor\s*=\s*MaterialTheme\.colorScheme\.surface\s*\)"""),
    Regex("""CardDefaults\.cardColors\(\s*containerColor\s*=\s*MaterialTheme\.colorScheme\.background(?:\.copy\([^)]*\))?\s*\)"""),
    Regex("""containerColor\s*=\s*MaterialTheme\.colorScheme\.surface(?:\.copy\([^)]*\))?"""),
    Regex("""containerColor\s*=\s*MaterialTheme\.colorScheme\.background(?:\.copy\([^)]*\))?"""),
    Regex("""\.background\(\s*MaterialTheme\.colorScheme\.background\s*\)"""),
    Regex("""Color\((0x[0-9A-Fa-f]+)\)"""),
    Regex("""Color\.(White|Black|Gray)\b""")
)

tasks.register("enforceSemanticSurfaceTokens") {
    group = "verification"
    description = "Enforces semantic surface usage in feature layer UI files."
    doLast {
        val scanTargets = listOf(
            file("src/main/java/com/horsegallop/feature"),
            file("src/main/java/com/horsegallop/core"),
            file("src/main/java/com/horsegallop/navigation"),
            file("src/main/java/com/horsegallop/MainActivity.kt")
        )

        val violations = mutableListOf<String>()
        scanTargets.forEach { target ->
            if (!target.exists()) return@forEach
            val files = if (target.isDirectory) {
                target.walkTopDown().filter { it.isFile && it.extension == "kt" && !it.name.contains(" 2") }.toList()
            } else {
                if (target.extension == "kt" && !target.name.contains(" 2")) listOf(target) else emptyList()
            }

            files.forEach { file ->
                val relativePath = file.relativeTo(project.projectDir).path
                file.readLines().forEachIndexed { index, line ->
                    val hasViolation = disallowedSurfacePatterns.any { regex -> regex.containsMatchIn(line) }
                    val allowTransparent = line.contains("Color.Transparent")
                    if (hasViolation && !allowTransparent) {
                        violations += "$relativePath:${index + 1}: ${line.trim()}"
                    }
                }
            }
        }

        if (violations.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("Found disallowed direct surface/background usage in UI layer:")
                    violations.forEach { appendLine(it) }
                    appendLine("Use LocalSemanticColors tokens (screenBase/screenTopBar/cardElevated/cardSubtle/panelOverlay/cardStroke) instead.")
                }
            )
        }
    }
}

tasks.named("preBuild").configure {
    dependsOn("enforceSemanticSurfaceTokens")
}

tasks.register("androidQualityConventions") {
    group = "verification"
    description = "Runs lightweight Android quality checks for changed Kotlin and resource files."
    doLast {
        exec {
            commandLine(
                "python3",
                rootProject.file("scripts/android_quality_checks.py").absolutePath,
                "--project-dir",
                rootProject.projectDir.absolutePath
            )
        }
    }
}

kapt {
    correctErrorTypes = true
    showProcessorStats = true
}

dependencies {
	coreLibraryDesugaring(libs.desugar.jdk.libs)
	implementation(platform(libs.compose.bom))
	implementation(libs.compose.ui)
	implementation(libs.compose.material3)
	implementation(libs.compose.tooling)
	implementation(libs.compose.navigation)
	implementation(libs.compose.icons.extended)
    implementation(libs.androidx.core.ktx)
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
    implementation("com.google.firebase:firebase-functions-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.firebase.appcheck.debug)
    implementation(libs.google.auth)
    implementation(libs.google.maps)
    implementation(libs.google.location)
    implementation(libs.maps.compose)
	implementation("androidx.core:core-splashscreen:1.0.1")
	implementation(libs.lottie.compose)
    implementation(libs.coil.compose)
    implementation(libs.shimmer.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi.kotlin)
    implementation("javax.inject:javax.inject:1")
    implementation("com.android.billingclient:billing-ktx:7.1.1")
	debugImplementation(libs.compose.tooling)
	testImplementation("junit:junit:4.13.2")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
	testImplementation("org.mockito:mockito-inline:5.2.0")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
	androidTestImplementation("androidx.test.ext:junit:1.2.1")
	androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
	androidTestImplementation(libs.compose.ui.test.junit4)
	debugImplementation(libs.compose.ui.test.manifest)
}
