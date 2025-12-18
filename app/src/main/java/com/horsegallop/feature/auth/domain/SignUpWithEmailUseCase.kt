package com.horsegallop.feature.auth.domain

import com.horsegallop.feature.auth.domain.model.User
import com.horsegallop.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignUpWithEmailUseCase @Inject constructor(
    private val repository: AuthRepository
) {

    fun execute(email: String, password: String, firstName: String, lastName: String): Flow<Result<User>> {
        return repository.signUpWithEmail(email, password, firstName, lastName)
    }

}
