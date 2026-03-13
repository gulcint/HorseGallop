package com.horsegallop.domain.settings.usecase

import com.horsegallop.domain.settings.model.UserSettings
import com.horsegallop.domain.settings.repository.UserSettingsRepository
import javax.inject.Inject

class UpdateUserSettingsUseCase @Inject constructor(
    private val repository: UserSettingsRepository
) {
    suspend operator fun invoke(settings: UserSettings): Result<Unit> =
        repository.updateUserSettings(settings)
}
