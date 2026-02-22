package com.horsegallop.domain.privacy.repository

import kotlinx.coroutines.flow.Flow

interface PrivacyRepository {
    fun exportUserData(): Flow<Result<String>>
    fun deleteUserData(): Flow<Result<Unit>>
}
