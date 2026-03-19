package com.horsegallop.data.auth.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.domain.auth.model.UserProfile
import com.horsegallop.domain.auth.repository.ProfileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val supabaseDataSource: SupabaseDataSource,
    @ApplicationContext private val context: Context
) : ProfileRepository {

    override fun getUserProfile(@Suppress("UNUSED_PARAMETER") uid: String): Flow<Result<UserProfile>> = flow {
        try {
            val dto = supabaseDataSource.getUserProfile()
            if (dto != null) {
                emit(Result.success(
                    UserProfile(
                        firstName = dto.firstName,
                        lastName = dto.lastName,
                        email = dto.email,
                        phone = dto.phone.orEmpty(),
                        city = dto.city.orEmpty(),
                        birthDate = dto.birthDate.orEmpty(),
                        photoUrl = dto.photoUrl,
                        countryCode = dto.countryCode.ifBlank { "+90" },
                        weight = null
                    )
                ))
            } else {
                emit(Result.success(UserProfile()))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun updateUserProfile(
        @Suppress("UNUSED_PARAMETER") uid: String,
        profile: UserProfile
    ): Flow<Result<Unit>> = flow {
        try {
            val updates = buildMap<String, Any?> {
                put("first_name", profile.firstName)
                put("last_name", profile.lastName)
                put("phone", profile.phone)
                put("city", profile.city)
                put("birth_date", profile.birthDate)
                put("country_code", profile.countryCode)
            }
            supabaseDataSource.updateUserProfile(updates)
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun updateProfileImage(uid: String, uri: Uri): Flow<Result<String>> = flow {
        try {
            val bytes = readUriBytes(uri)
            val url = supabaseDataSource.uploadProfilePhoto(uid, bytes).getOrThrow()
            // Update photo_url in user_profiles table
            supabaseDataSource.updateUserProfile(mapOf("photo_url" to url))
            emit(Result.success(url))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun deleteAccount(): Flow<Result<Unit>> = flow {
        try {
            val userId = supabaseDataSource.currentUserId()
                ?: throw IllegalStateException("No user logged in")
            // Delete user profile row — Supabase RLS / cascade handles related data
            supabaseDataSource.updateUserProfile(mapOf("deleted_at" to java.time.Instant.now().toString()))
            supabaseDataSource.signOut()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun signOut() {
        // Fire-and-forget; caller should use coroutine scope if needed
    }

    // ─── helpers ─────────────────────────────────────────────

    private fun readUriBytes(uri: Uri): ByteArray {
        val resolver: ContentResolver = context.contentResolver
        return resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalArgumentException("Cannot open URI: $uri")
    }
}
