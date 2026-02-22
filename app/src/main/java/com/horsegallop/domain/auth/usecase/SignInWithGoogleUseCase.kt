package com.horsegallop.domain.auth.usecase

import com.horsegallop.domain.auth.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun execute(idToken: String) {
        authRepository.signInWithGoogleIdToken(idToken)
    }
}

