package com.horsegallop.data.settings.repository

import com.horsegallop.data.remote.functions.AppFunctionsDataSource
import com.horsegallop.domain.settings.model.UserSettings
import com.horsegallop.domain.settings.repository.UserSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepositoryImpl @Inject constructor(
    private val functionsDataSource: AppFunctionsDataSource
) : UserSettingsRepository {

    override suspend fun getUserSettings(): Result<UserSettings> = runCatching {
        val dto = functionsDataSource.getUserSettings()
        UserSettings(
            themeMode = dto.themeMode,
            language = dto.language,
            notificationsEnabled = dto.notificationsEnabled,
            weightUnit = dto.weightUnit,
            distanceUnit = dto.distanceUnit
        )
    }

    override suspend fun updateUserSettings(settings: UserSettings): Result<Unit> = runCatching {
        functionsDataSource.updateUserSettings(
            themeMode = settings.themeMode,
            language = settings.language,
            notificationsEnabled = settings.notificationsEnabled,
            weightUnit = settings.weightUnit,
            distanceUnit = settings.distanceUnit
        )
    }
}
