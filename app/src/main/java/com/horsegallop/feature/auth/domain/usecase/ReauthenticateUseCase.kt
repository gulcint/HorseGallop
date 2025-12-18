package com.horsegallop.feature.auth.domain.usecase

import com.google.firebase.auth.AuthCredential
import com.horsegallop.feature.auth.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReauthenticateUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(credential: AuthCredential): Flow<Result<Unit>> {
        return repository.reauthenticate(credential)
    }
}
