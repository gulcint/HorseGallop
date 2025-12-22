package com.horsegallop.feature.auth.di

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.horsegallop.R
import com.horsegallop.core.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideGoogleSignInOptions(@ApplicationContext context: Context): GoogleSignInOptions =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

    @Provides
    @Singleton
    fun provideGoogleSignInClient(
        @ApplicationContext context: Context,
        gso: GoogleSignInOptions
    ): GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance(Constants.FIREBASE_DB_NAME)

    @Provides
    @Singleton
    fun provideFirebaseStorage(): com.google.firebase.storage.FirebaseStorage = com.google.firebase.storage.FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): com.horsegallop.data.auth.FirebaseAuthRepository =
        com.horsegallop.data.auth.FirebaseAuthRepository(auth, firestore)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthBinderModule {
    @dagger.Binds
    @Singleton
    abstract fun bindAuthRepository(
        firebaseAuthRepository: com.horsegallop.data.auth.FirebaseAuthRepository
    ): com.horsegallop.domain.auth.AuthRepository

    @dagger.Binds
    @Singleton
    abstract fun bindProfileRepository(
        profileRepositoryImpl: com.horsegallop.feature.auth.data.repository.ProfileRepositoryImpl
    ): com.horsegallop.feature.auth.domain.repository.ProfileRepository
}
