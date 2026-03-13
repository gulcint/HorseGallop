package com.horsegallop.domain.settings.model

data class UserSettings(
    val themeMode: String = "SYSTEM",
    val language: String = "SYSTEM",
    val notificationsEnabled: Boolean = true,
    val weightUnit: String = "kg",
    val distanceUnit: String = "km"
)
