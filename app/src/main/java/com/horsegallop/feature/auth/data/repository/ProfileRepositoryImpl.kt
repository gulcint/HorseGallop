package com.horsegallop.feature.auth.data.repository

import android.net.Uri
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.horsegallop.feature.auth.domain.model.UserProfile
import com.horsegallop.feature.auth.domain.repository.ProfileRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ProfileRepository {

    override fun getUserProfile(uid: String): Flow<Result<UserProfile>> = flow {
        try {
            val doc = firestore.collection("users").document(uid).get().await()
            val profile = UserProfile(
                firstName = doc.getString("firstName") ?: "",
                lastName = doc.getString("lastName") ?: "",
                email = doc.getString("email") ?: (auth.currentUser?.email ?: ""),
                phone = doc.getString("phone") ?: "",
                city = doc.getString("city") ?: "",
                birthDate = doc.getString("birthDate") ?: "",
                photoUrl = doc.getString("photoUrl"),
                countryCode = doc.getString("countryCode") ?: "+90"
            )
            
            // Handle Timestamp if present
            val ts = doc.getTimestamp("birthDate")
            val finalProfile = if (ts != null) {
                val cal = java.util.Calendar.getInstance()
                cal.time = ts.toDate()
                val y = cal.get(java.util.Calendar.YEAR)
                val m = cal.get(java.util.Calendar.MONTH) + 1
                val d = cal.get(java.util.Calendar.DAY_OF_MONTH)
                profile.copy(birthDate = String.format("%04d-%02d-%02d", y, m, d))
            } else {
                profile
            }
            
            emit(Result.success(finalProfile))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun updateUserProfile(uid: String, profile: UserProfile): Flow<Result<Unit>> = flow {
        try {
            val updates = mapOf(
                "firstName" to profile.firstName,
                "lastName" to profile.lastName,
                "phone" to profile.phone,
                "city" to profile.city,
                "birthDate" to profile.birthDate,
                "countryCode" to profile.countryCode
            )
            firestore.collection("users").document(uid).update(updates).await()
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
            
            // Delete Firestore Data
            firestore.collection("users").document(user.uid).delete().await()
            
            // Delete Photo
            user.photoUrl?.let {
                try {
                    storage.getReferenceFromUrl(it.toString()).delete().await()
                } catch (e: Exception) {
                    // Ignore if photo doesn't exist
                }
            }

            // Delete User
            user.delete().await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun reauthenticate(credential: AuthCredential): Flow<Result<Unit>> = flow {
        try {
            val user = auth.currentUser ?: throw Exception("No user logged in")
            user.reauthenticate(credential).await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun signOut() {
        auth.signOut()
    }
}
