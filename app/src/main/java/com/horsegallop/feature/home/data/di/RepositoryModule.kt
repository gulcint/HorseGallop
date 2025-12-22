package com.horsegallop.feature.home.data.di

import com.horsegallop.feature.home.data.repository.HomeRepositoryImpl
import com.horsegallop.feature.home.domain.repository.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
  @Binds @Singleton abstract fun bindHomeRepository(impl: HomeRepositoryImpl): HomeRepository
}
