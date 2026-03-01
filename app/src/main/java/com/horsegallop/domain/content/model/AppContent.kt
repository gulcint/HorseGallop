package com.horsegallop.domain.content.model

data class AppContent(
    val locale: String,
    val homeHeroTitle: String? = null,
    val homeHeroSubtitle: String? = null,
    val offlineHelp: String? = null
)
