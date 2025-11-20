package com.horsegallop.feature.auth.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    suspend fun signInWithGoogleIdToken(idToken: String)
    fun isSignedIn(): Boolean
    fun signOut()
}

class FirebaseAuthRepository(
    private val auth: FirebaseAuth
) : AuthRepository {
    override suspend fun signInWithGoogleIdToken(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
    }

    override fun isSignedIn(): Boolean = auth.currentUser != null

    override fun signOut() {
        auth.signOut()
    }
}


