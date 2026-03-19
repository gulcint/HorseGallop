package com.horsegallop.data.settings.repository

import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.data.remote.supabase.SupabaseUserSettingsDto
import com.horsegallop.domain.settings.model.UserSettings
import com.horsegallop.domain.settings.repository.UserSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepositoryImpl @Inject constructor(
    private val supabaseDataSource: SupabaseDataSource
) : UserSettingsRepository {

    override suspend fun getUserSettings(): Result<UserSettings> = runCatching {
        val dto = supabaseDataSource.getUserSettingsById()
        UserSettings(
            themeMode = dto?.themeMode ?: "SYSTEM",
            language = dto?.language ?: "SYSTEM",
            notificationsEnabled = dto?.notificationsEnabled ?: true,
            weightUnit = dto?.weightUnit ?: "kg",
            distanceUnit = dto?.distanceUnit ?: "km"
        )
    }

    override suspend fun updateUserSettings(settings: UserSettings): Result<Unit> = runCatching {
        val uid = supabaseDataSource.currentUserId() ?: error("Not authenticated")
        val dto = SupabaseUserSettingsDto(
            userId = uid,
            themeMode = settings.themeMode,
            language = settings.language,
            notificationsEnabled = settings.notificationsEnabled,
            weightUnit = settings.weightUnit,
            distanceUnit = settings.distanceUnit
        )
        supabaseDataSource.upsertUserSettings(dto)
    }
}
