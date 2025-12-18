package com.horsegallop.feature.auth.domain.usecase

import android.net.Uri
import com.horsegallop.feature.auth.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateProfileImageUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(uid: String, uri: Uri): Flow<Result<String>> {
        return repository.updateProfileImage(uid, uri)
    }
}
