package com.horsegallop.data.auth.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.horsegallop.domain.auth.model.UserProfile
import com.horsegallop.domain.auth.repository.ProfileRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: com.google.firebase.storage.FirebaseStorage
) : ProfileRepository {

    override fun getUserProfile(uid: String): Flow<Result<UserProfile>> = callbackFlow {
        val docRef = firestore.collection("users").document(uid)
        
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            
            if (snapshot != null) {
                val currentUser = auth.currentUser
                var fName = snapshot.getString("firstName") ?: ""
                var lName = snapshot.getString("lastName") ?: ""
                
                // Fallback to Firebase Auth Display Name if Firestore data is missing
                if (fName.isBlank() && lName.isBlank() && currentUser?.displayName.isNullOrBlank().not()) {
                    val parts = currentUser!!.displayName!!.trim().split(" ")
                    if (parts.isNotEmpty()) {
                        fName = parts.first()
                        if (parts.size > 1) {
                            lName = parts.drop(1).joinToString(" ")
                        }
                    }
                }

                val birthDateStr = try {
                    val ts = snapshot.getTimestamp("birthDate")
                    if (ts != null) {
                        val cal = java.util.Calendar.getInstance()
                        cal.time = ts.toDate()
                        val y = cal.get(java.util.Calendar.YEAR)
                        val m = cal.get(java.util.Calendar.MONTH) + 1
                        val d = cal.get(java.util.Calendar.DAY_OF_MONTH)
                        String.format("%04d-%02d-%02d", y, m, d)
                    } else {
                        val millis = snapshot.getLong("birthDate")
                        if (millis != null) {
                            val cal = java.util.Calendar.getInstance()
                            cal.timeInMillis = millis
                            val y = cal.get(java.util.Calendar.YEAR)
                            val m = cal.get(java.util.Calendar.MONTH) + 1
                            val d = cal.get(java.util.Calendar.DAY_OF_MONTH)
                            String.format("%04d-%02d-%02d", y, m, d)
                        } else {
                            snapshot.getString("birthDate") ?: ""
                        }
                    }
                } catch (e: Exception) {
                    ""
                }

                val profile = UserProfile(
                    firstName = fName,
                    lastName = lName,
                    email = snapshot.getString("email") ?: (currentUser?.email ?: ""),
                    phone = snapshot.getString("phone") ?: "",
                    city = snapshot.getString("city") ?: "",
                    birthDate = birthDateStr,
                    photoUrl = snapshot.getString("photoUrl"),
                    countryCode = snapshot.getString("countryCode") ?: "+90",
                    weight = snapshot.getDouble("weight")?.toFloat()
                )

                if (!snapshot.exists()) {
                    // Try initialize if doesn't exist (non-blocking)
                    val birthDateTimestamp = if (profile.birthDate.isNotBlank()) {
                        try {
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            val date = sdf.parse(profile.birthDate)
                            if (date != null) com.google.firebase.Timestamp(date) else null
                        } catch (e: Exception) { null }
                    } else null

                    val init = hashMapOf(
                        "firstName" to profile.firstName,
                        "lastName" to profile.lastName,
                        "email" to profile.email,
                        "phone" to profile.phone,
                        "city" to profile.city,
                        "birthDate" to birthDateTimestamp,
                        "photoUrl" to profile.photoUrl,
                        "countryCode" to profile.countryCode,
                        "weight" to profile.weight
                    )
                    docRef.set(init, com.google.firebase.firestore.SetOptions.merge())
                }
                
                trySend(Result.success(profile))
            }
        }
        
        awaitClose { listener.remove() }
    }

    override fun updateUserProfile(uid: String, profile: UserProfile): Flow<Result<Unit>> = flow {
        try {
            val birthDateTimestamp = if (profile.birthDate.isNotBlank()) {
                try {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val date = sdf.parse(profile.birthDate)
                    if (date != null) com.google.firebase.Timestamp(date) else null
                } catch (e: Exception) { null }
            } else null

            val sanitizedPhone = profile.phone.filter { it.isDigit() }
            val displayName = listOf(profile.firstName, profile.lastName).filter { it.isNotBlank() }.joinToString(" ")
            val updates = hashMapOf<String, Any?>(
                "firstName" to profile.firstName,
                "lastName" to profile.lastName,
                "name" to displayName,
                "phone" to sanitizedPhone,
                "city" to profile.city,
                "birthDate" to birthDateTimestamp,
                "countryCode" to profile.countryCode,
                "weight" to profile.weight
            )
            firestore.collection("users").document(uid).set(updates, com.google.firebase.firestore.SetOptions.merge()).await()
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

    override fun signOut() {
        auth.signOut()
    }
}
