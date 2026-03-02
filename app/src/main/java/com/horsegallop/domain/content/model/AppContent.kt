package com.horsegallop.domain.content.model

data class AppContent(
    val locale: String,
    val homeHeroTitle: String? = null,
    val homeHeroSubtitle: String? = null,
    val offlineHelp: String? = null,
    val loginTitle: String? = null,
    val loginSubtitle: String? = null,
    val emailLoginTitle: String? = null,
    val emailLoginSubtitle: String? = null,
    val enrollTitle: String? = null,
    val enrollSubtitle: String? = null,
    val forgotPasswordSubtitle: String? = null,
    val onboardingHeroTitle: String? = null,
    val onboardingHeroSubtitle: String? = null,
    val onboardingHelpText: String? = null,
    val rideLiveTitle: String? = null,
    val rideLiveSubtitleIdle: String? = null,
    val rideLiveSubtitleActive: String? = null,
    val ridePermissionTitle: String? = null,
    val ridePermissionHint: String? = null,
    val rideGrantLocationCta: String? = null,
    val settingsThemeSubtitle: String? = null,
    val settingsLanguageSubtitle: String? = null,
    val settingsNotificationsSubtitle: String? = null,
    val settingsPrivacySubtitle: String? = null
)
