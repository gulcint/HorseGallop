package com.horsegallop.data.privacy.repository

import com.horsegallop.data.remote.ApiService
import com.horsegallop.domain.privacy.repository.PrivacyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PrivacyRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : PrivacyRepository {

    override fun exportUserData(): Flow<Result<String>> = flow {
        try {
            val json = withContext(Dispatchers.IO) {
                apiService.exportUserData().string()
            }
            emit(Result.success(json))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun deleteUserData(): Flow<Result<Unit>> = flow {
        try {
            withContext(Dispatchers.IO) {
                apiService.deleteUserData()
            }
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
