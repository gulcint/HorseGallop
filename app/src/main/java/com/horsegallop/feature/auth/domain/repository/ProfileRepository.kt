package com.horsegallop.feature.auth.domain.repository

import android.net.Uri
import com.google.firebase.auth.AuthCredential
import com.horsegallop.feature.auth.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getUserProfile(uid: String): Flow<Result<UserProfile>>
    fun updateUserProfile(uid: String, profile: UserProfile): Flow<Result<Unit>>
    fun updateProfileImage(uid: String, uri: Uri): Flow<Result<String>>
    fun deleteAccount(): Flow<Result<Unit>>
    fun reauthenticate(credential: AuthCredential): Flow<Result<Unit>>
    fun signOut()
}
