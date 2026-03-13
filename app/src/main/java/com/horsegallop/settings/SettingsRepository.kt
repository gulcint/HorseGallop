package com.horsegallop.settings

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val prefs: SharedPreferences
) {
    private val _state = MutableStateFlow(readState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == KEY_THEME_MODE || key == KEY_LANGUAGE || key == KEY_NOTIFICATIONS) {
            _state.value = readState()
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.id).apply()
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.id).apply()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
    }

    fun replaceState(
        themeMode: ThemeMode,
        language: AppLanguage,
        notificationsEnabled: Boolean
    ) {
        prefs.edit()
            .putString(KEY_THEME_MODE, themeMode.id)
            .putString(KEY_LANGUAGE, language.id)
            .putBoolean(KEY_NOTIFICATIONS, notificationsEnabled)
            .apply()
    }

    private fun readState(): SettingsState {
        val themeMode = ThemeMode.fromId(prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.id))
        val language = AppLanguage.fromId(prefs.getString(KEY_LANGUAGE, AppLanguage.SYSTEM.id))
        val notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, true)
        return SettingsState(
            themeMode = themeMode,
            language = language,
            notificationsEnabled = notificationsEnabled
        )
    }

    companion object {
        const val KEY_THEME_MODE = "settings.theme_mode"
        const val KEY_LANGUAGE = "settings.language"
        const val KEY_NOTIFICATIONS = "settings.notifications"
    }
}
