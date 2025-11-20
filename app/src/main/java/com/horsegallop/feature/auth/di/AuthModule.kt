package com.horsegallop.feature.auth.di

import com.google.firebase.auth.FirebaseAuth
import com.horsegallop.feature.auth.data.AuthRepository
import com.horsegallop.feature.auth.data.FirebaseAuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    fun provideAuthRepository(auth: FirebaseAuth): AuthRepository = FirebaseAuthRepository(auth)
}


