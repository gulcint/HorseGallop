package com.horsegallop.domain.auth

interface AuthRepository {
    suspend fun signInWithGoogleIdToken(idToken: String)
    fun isSignedIn(): Boolean
    fun signOut()
}

