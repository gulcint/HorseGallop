package com.horsegallop.feature.barn.di

import com.horsegallop.feature.barn.data.repository.BarnRepositoryImpl
import com.horsegallop.feature.barn.domain.repository.BarnRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BarnModule {
    @Binds
    @Singleton
    abstract fun bindBarnRepository(impl: BarnRepositoryImpl): BarnRepository
}
