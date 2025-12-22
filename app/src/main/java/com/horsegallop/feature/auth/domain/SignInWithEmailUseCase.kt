package com.horsegallop.feature.auth.domain

import com.horsegallop.domain.model.User
import com.horsegallop.domain.auth.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignInWithEmailUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    fun execute(email: String, password: String): Flow<Result<User>> {
        return repository.signInWithEmail(email, password)
    }
}
