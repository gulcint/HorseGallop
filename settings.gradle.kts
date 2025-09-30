pluginManagement {
	repositories {
		gradlePluginPortal()
		google()
		mavenCentral()
	}
}

dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google()
		mavenCentral()
	}
}

rootProject.name = "adincountry"

include(
	":app",
	":core",
	":domain",
	":data",
	":feature_auth",
	":feature_home",
	":feature_schedule",
	":feature_reservation",
	":feature_orders",
	":feature_reviews",
	":feature_admin",
	":feature_profile",
	":feature_settings"
)
