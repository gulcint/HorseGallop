package com.horsegallop.data.auth

import com.horsegallop.data.remote.supabase.SupabaseAuthDataSource
import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.data.remote.supabase.SupabaseUserProfileDto
import com.horsegallop.domain.auth.AuthRepository
import com.horsegallop.domain.auth.model.UserProfile
import com.horsegallop.domain.model.User
import com.horsegallop.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Supabase-backed implementation of [AuthRepository].
 * Handles email/password auth, Google OAuth (native token exchange), and user profile management.
 *
 * Methods that were Firebase-specific (lottie config, splash texts, confirmPasswordReset)
 * return stub/no-op values — these will be migrated or removed in a future sprint.
 */
class SupabaseAuthRepositoryImpl @Inject constructor(
    private val authDataSource: SupabaseAuthDataSource,
    private val supabaseDataSource: SupabaseDataSource
) : AuthRepository {

    // ─── GOOGLE SIGN IN ───────────────────────────────────────────────────────

    override suspend fun signInWithGoogleIdToken(idToken: String) {
        authDataSource.signInWithGoogleIdToken(idToken).getOrThrow()
    }

    // ─── STATE ────────────────────────────────────────────────────────────────

    override fun isSignedIn(): Boolean = authDataSource.isSignedIn()

    override suspend fun signOut() {
        authDataSource.signOut()
    }

    override fun getCurrentUserId(): String? = authDataSource.getCurrentUserId()

    // ─── EMAIL AUTH ───────────────────────────────────────────────────────────

    override fun signUpWithEmail(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Flow<Result<User>> = flow {
        val result = authDataSource.signUpWithEmail(email, password, firstName, lastName)
        emit(result.map { userInfo ->
            User(
                id = userInfo.id,
                role = UserRole.CUSTOMER,
                name = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" "),
                email = userInfo.email ?: email,
                isEmailVerified = userInfo.emailConfirmedAt != null,
                locale = null,
                lastVisitIso = null
            )
        })
    }

    override fun signInWithEmail(email: String, password: String): Flow<Result<User>> = flow {
        val result = authDataSource.signInWithEmail(email, password)
        emit(result.map { userInfo ->
            val profile = runCatching { supabaseDataSource.getUserProfile() }.getOrNull()
            User(
                id = userInfo.id,
                role = UserRole.CUSTOMER,
                name = profile?.let {
                    listOf(it.firstName, it.lastName).filter { n -> n.isNotBlank() }.joinToString(" ")
                } ?: (userInfo.email ?: ""),
                email = userInfo.email ?: email,
                isEmailVerified = userInfo.emailConfirmedAt != null,
                locale = profile?.countryCode,
                lastVisitIso = null
            )
        })
    }

    // ─── PASSWORD RESET ───────────────────────────────────────────────────────

    override fun sendPasswordResetEmail(email: String): Flow<Result<Unit>> = flow {
        emit(authDataSource.resetPassword(email))
    }

    override fun confirmPasswordReset(email: String, code: String, newPassword: String): Flow<Result<Unit>> = flow {
        val verifyResult = authDataSource.verifyOtp(email, code)
        if (verifyResult.isFailure) {
            emit(Result.failure(verifyResult.exceptionOrNull() ?: Exception("Invalid or expired recovery code")))
            return@flow
        }
        emit(authDataSource.updatePassword(newPassword))
    }

    override fun resendVerificationEmail(email: String?, password: String?): Flow<Result<Unit>> = flow {
        if (email.isNullOrBlank()) {
            emit(Result.failure(IllegalArgumentException("Email is required to resend verification")))
            return@flow
        }
        emit(authDataSource.resendVerificationEmail(email))
    }

    override fun checkEmailVerified(): Flow<Result<Boolean>> = flow {
        val user = authDataSource.getCurrentUser()
        emit(Result.success(user?.emailConfirmedAt != null))
    }

    // ─── PROFILE ─────────────────────────────────────────────────────────────

    override fun saveUserToRemote(user: UserProfile): Flow<Result<Unit>> = flow {
        val uid = authDataSource.getCurrentUserId()
        if (uid == null) {
            emit(Result.failure(Exception("No authenticated user")))
            return@flow
        }
        val updates = buildMap<String, Any?> {
            put("first_name", user.firstName)
            put("last_name", user.lastName)
            put("email", user.email)
            put("phone", user.phone.ifBlank { null })
            put("city", user.city.ifBlank { null })
            put("birth_date", user.birthDate.ifBlank { null })
            put("photo_url", user.photoUrl)
            put("country_code", user.countryCode)
            put("weight_kg", user.weight?.toDouble())
        }
        val result = runCatching { supabaseDataSource.updateUserProfile(updates) }
        emit(result)
    }

    // ─── CONTENT (Firebase-only stubs) ───────────────────────────────────────

    override fun getLottieConfig(): Flow<Result<Pair<String, String>>> = flow {
        // Lottie config migration to Supabase app_content table is planned for Sprint 3.
        emit(
            Result.success(
                "https://assets9.lottiefiles.com/packages/lf20_jbrw3hcz.json" to
                    "https://assets9.lottiefiles.com/packages/lf20_yYdx1X.json"
            )
        )
    }

    override fun getSplashTexts(locale: String): Flow<Result<Pair<String, String>>> = flow {
        val result = runCatching {
            val lang = locale.lowercase()
            val content = supabaseDataSource.getAppContent(lang)
            val title = content.firstOrNull { it.key == "splash_title" }?.value ?: ""
            val subtitle = content.firstOrNull { it.key == "splash_subtitle" }?.value ?: ""
            title to subtitle
        }
        emit(result)
    }
}
