package com.horsegallop.data.safety.repository

import com.horsegallop.data.remote.functions.AppFunctionsDataSource
import com.horsegallop.domain.safety.model.SafetyContact
import com.horsegallop.domain.safety.model.SafetySettings
import com.horsegallop.domain.safety.repository.SafetyRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SafetyRepositoryImpl @Inject constructor(
    private val functionsDataSource: AppFunctionsDataSource
) : SafetyRepository {

    override suspend fun getSafetySettings(): Result<SafetySettings> = runCatching {
        val dto = functionsDataSource.getSafetySettings()
        SafetySettings(
            isEnabled = dto.isEnabled,
            contacts = dto.contacts.map { c ->
                SafetyContact(id = c.id, name = c.name, phone = c.phone)
            },
            autoAlarmMinutes = dto.autoAlarmMinutes
        )
    }

    override suspend fun updateSafetyEnabled(isEnabled: Boolean): Result<Unit> = runCatching {
        functionsDataSource.updateSafetyEnabled(isEnabled)
    }

    override suspend fun addSafetyContact(name: String, phone: String): Result<SafetyContact> =
        runCatching {
            val dto = functionsDataSource.addSafetyContact(name, phone)
            SafetyContact(id = dto.id, name = dto.name, phone = dto.phone)
        }

    override suspend fun removeSafetyContact(contactId: String): Result<Unit> = runCatching {
        functionsDataSource.removeSafetyContact(contactId)
    }

    override suspend fun triggerSafetyAlarm(lat: Double, lng: Double): Result<Unit> = runCatching {
        functionsDataSource.triggerSafetyAlarm(lat, lng)
    }
}
