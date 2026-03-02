package com.horsegallop.domain.content.model

data class AppContent(
    val locale: String,
    val homeHeroTitle: String? = null,
    val homeHeroSubtitle: String? = null,
    val offlineHelp: String? = null,
    val loginTitle: String? = null,
    val loginSubtitle: String? = null,
    val forgotPasswordSubtitle: String? = null,
    val onboardingHeroTitle: String? = null,
    val onboardingHeroSubtitle: String? = null
)
