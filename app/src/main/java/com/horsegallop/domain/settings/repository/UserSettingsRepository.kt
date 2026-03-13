package com.horsegallop.domain.settings.repository

import com.horsegallop.domain.settings.model.UserSettings

interface UserSettingsRepository {
    suspend fun getUserSettings(): Result<UserSettings>
    suspend fun updateUserSettings(settings: UserSettings): Result<Unit>
}
