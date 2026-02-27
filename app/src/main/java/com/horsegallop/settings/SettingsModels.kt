package com.horsegallop.settings

import androidx.core.os.LocaleListCompat

enum class ThemeMode(val id: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromId(id: String?): ThemeMode {
            return entries.firstOrNull { it.id == id } ?: SYSTEM
        }
    }
}

enum class AppLanguage(val id: String, val localeTag: String?) {
    SYSTEM("system", null),
    ENGLISH("en", "en"),
    TURKISH("tr", "tr");

    companion object {
        fun fromId(id: String?): AppLanguage {
            return entries.firstOrNull { it.id == id } ?: SYSTEM
        }
    }
}

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val notificationsEnabled: Boolean = true
)

fun AppLanguage.toLocaleList(): LocaleListCompat {
    return if (this == AppLanguage.SYSTEM) {
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.forLanguageTags(localeTag ?: "")
    }
}
