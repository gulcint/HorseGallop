package com.horsegallop.data.di

import com.horsegallop.data.repository.AuthRepositoryImpl
import com.horsegallop.data.repository.HomeRepositoryImpl
import com.horsegallop.domain.repository.AuthRepository
import com.horsegallop.domain.repository.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
  @Binds @Singleton abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
  @Binds @Singleton abstract fun bindHomeRepository(impl: HomeRepositoryImpl): HomeRepository
}
