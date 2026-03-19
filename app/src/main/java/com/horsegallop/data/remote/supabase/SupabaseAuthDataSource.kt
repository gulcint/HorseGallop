package com.horsegallop.data.remote.supabase

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Low-level Supabase Auth data source.
 * All operations return [Result] for safe error propagation without try-catch at call sites.
 * Repositories compose these results into domain flows.
 */
@Singleton
class SupabaseAuthDataSource @Inject constructor(
    private val auth: Auth
) {

    // ─── SIGN IN ─────────────────────────────────────────────────────────────

    suspend fun signInWithEmail(email: String, password: String): Result<UserInfo> = runCatching {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        auth.currentUserOrNull() ?: error("Sign in succeeded but no user returned")
    }

    // ─── SIGN UP ─────────────────────────────────────────────────────────────

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        firstName: String = "",
        lastName: String = ""
    ): Result<UserInfo> = runCatching {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("first_name", firstName)
                put("last_name", lastName)
                put("full_name", listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" "))
            }
        }
        auth.currentUserOrNull() ?: error("Sign up succeeded but no user returned")
    }

    // ─── SIGN OUT ────────────────────────────────────────────────────────────

    suspend fun signOut(): Result<Unit> = runCatching {
        auth.signOut()
    }

    // ─── PASSWORD RESET ───────────────────────────────────────────────────────

    suspend fun resetPassword(email: String): Result<Unit> = runCatching {
        auth.resetPasswordForEmail(email)
    }

    /**
     * Updates the password of the currently authenticated user.
     * Called after the user follows the password-reset deep link and a session is established.
     */
    suspend fun updatePassword(newPassword: String): Result<Unit> = runCatching {
        auth.updateUser {
            password = newPassword
        }
    }

    // ─── CURRENT USER ────────────────────────────────────────────────────────

    fun getCurrentUser(): UserInfo? = auth.currentUserOrNull()

    fun getCurrentUserId(): String? = auth.currentUserOrNull()?.id

    fun getCurrentUserEmail(): String? = auth.currentUserOrNull()?.email

    fun isSignedIn(): Boolean = auth.currentUserOrNull() != null

    // ─── AUTH STATE FLOW ─────────────────────────────────────────────────────

    /**
     * Emits [UserInfo] when signed in, null when signed out.
     * Collect this in repositories that need to react to auth state changes.
     */
    fun getAuthStateFlow(): Flow<UserInfo?> =
        auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> auth.currentUserOrNull()
                else -> null
            }
        }

    // ─── GOOGLE OAUTH ────────────────────────────────────────────────────────

    /**
     * Signs in with a Google ID token obtained via the legacy GoogleSignIn API on Android.
     * Uses the IDToken provider with Google as the OIDC provider — Supabase SDK 3.x pattern.
     */
    suspend fun signInWithGoogleIdToken(idToken: String): Result<UserInfo> = runCatching {
        auth.signInWith(IDToken) {
            this.idToken = idToken
            this.provider = Google
        }
        auth.currentUserOrNull() ?: error("Google sign-in succeeded but no user returned")
    }

    // ─── TOKEN REFRESH ───────────────────────────────────────────────────────

    suspend fun refreshSession(): Result<Unit> = runCatching {
        auth.refreshCurrentSession()
    }
}
