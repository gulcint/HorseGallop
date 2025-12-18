package com.horsegallop.feature.auth.domain.usecase

import com.horsegallop.feature.auth.domain.repository.ProfileRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke() {
        repository.signOut()
    }
}
