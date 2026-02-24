package com.horsegallop.data.auth.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import com.horsegallop.domain.auth.model.UserProfile
import com.horsegallop.domain.auth.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val functions: FirebaseFunctions
) : ProfileRepository {

    override fun getUserProfile(@Suppress("UNUSED_PARAMETER") uid: String): Flow<Result<UserProfile>> = flow {
        try {
            val result = functions
                .getHttpsCallable("getUserProfile")
                .call()
                .await()

            val payload = result.data as? Map<*, *> ?: emptyMap<String, Any?>()
            val profile = mapPayloadToProfile(payload)

            emit(Result.success(profile))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun updateUserProfile(
        @Suppress("UNUSED_PARAMETER") uid: String,
        profile: UserProfile
    ): Flow<Result<Unit>> = flow {
        try {
            val request = hashMapOf(
                "firstName" to profile.firstName,
                "lastName" to profile.lastName,
                "phone" to profile.phone,
                "city" to profile.city,
                "birthDate" to profile.birthDate,
                "countryCode" to profile.countryCode,
                "weight" to profile.weight
            )

            functions
                .getHttpsCallable("updateUserProfile")
                .call(request)
                .await()

            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun updateProfileImage(uid: String, uri: Uri): Flow<Result<String>> = flow {
        try {
            val ref = storage.reference.child("profiles/$uid.jpg")
            ref.putFile(uri).await()
            val url = ref.downloadUrl.await().toString()
            firestore.collection("users").document(uid).update("photoUrl", url).await()
            emit(Result.success(url))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun deleteAccount(): Flow<Result<Unit>> = flow {
        try {
            val user = auth.currentUser ?: throw Exception("No user logged in")

            firestore.collection("users").document(user.uid).delete().await()

            user.photoUrl?.let {
                try {
                    storage.getReferenceFromUrl(it.toString()).delete().await()
                } catch (_: Exception) {
                    // Ignore missing photo objects.
                }
            }

            user.delete().await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun signOut() {
        auth.signOut()
    }

    private fun mapPayloadToProfile(payload: Map<*, *>): UserProfile {
        val currentUser = auth.currentUser

        var firstName = payload["firstName"] as? String ?: ""
        var lastName = payload["lastName"] as? String ?: ""

        if (firstName.isBlank() && lastName.isBlank() && !currentUser?.displayName.isNullOrBlank()) {
            val parts = currentUser!!.displayName!!.trim().split(" ")
            if (parts.isNotEmpty()) {
                firstName = parts.first()
                if (parts.size > 1) {
                    lastName = parts.drop(1).joinToString(" ")
                }
            }
        }

        val emailFromPayload = payload["email"] as? String
        val email = when {
            !emailFromPayload.isNullOrBlank() -> emailFromPayload
            !currentUser?.email.isNullOrBlank() -> currentUser?.email ?: ""
            else -> ""
        }

        val weight = when (val rawWeight = payload["weight"]) {
            is Number -> rawWeight.toFloat()
            else -> null
        }

        return UserProfile(
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = payload["phone"] as? String ?: "",
            city = payload["city"] as? String ?: "",
            birthDate = payload["birthDate"] as? String ?: "",
            photoUrl = payload["photoUrl"] as? String,
            countryCode = (payload["countryCode"] as? String).orEmpty().ifBlank { "+90" },
            weight = weight
        )
    }
}
