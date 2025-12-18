package com.horsegallop.feature.auth.domain.usecase

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class GetCurrentUserIdUseCase @Inject constructor(
    private val auth: FirebaseAuth
) {
    operator fun invoke(): String? {
        return auth.currentUser?.uid
    }
}
