package com.horsegallop.feature.auth.domain.usecase

import com.horsegallop.feature.auth.domain.model.UserProfile
import com.horsegallop.feature.auth.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(uid: String): Flow<Result<UserProfile>> {
        return repository.getUserProfile(uid)
    }
}
