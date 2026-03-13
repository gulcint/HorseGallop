package com.horsegallop.domain.safety.repository

import com.horsegallop.domain.safety.model.SafetyContact
import com.horsegallop.domain.safety.model.SafetySettings

interface SafetyRepository {
    suspend fun getSafetySettings(): Result<SafetySettings>
    suspend fun updateSafetyEnabled(isEnabled: Boolean): Result<Unit>
    suspend fun addSafetyContact(name: String, phone: String): Result<SafetyContact>
    suspend fun removeSafetyContact(contactId: String): Result<Unit>
    suspend fun triggerSafetyAlarm(lat: Double, lng: Double): Result<Unit>
}
