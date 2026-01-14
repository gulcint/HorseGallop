package com.horsegallop.domain.auth.usecase

import com.horsegallop.domain.auth.model.UserProfile
import com.horsegallop.domain.auth.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(uid: String): Flow<Result<UserProfile>> {
        return repository.getUserProfile(uid)
    }
}
