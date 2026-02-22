package com.horsegallop.domain.auth.usecase

import com.horsegallop.domain.auth.model.UserProfile
import com.horsegallop.domain.auth.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(uid: String, profile: UserProfile): Flow<Result<Unit>> {
        return repository.updateUserProfile(uid, profile)
    }
}
