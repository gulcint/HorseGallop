package com.example.data.di

import com.example.domain.repository.HomeRepository
import com.example.domain.usecase.GetSliderUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
  
  @Provides
  @Singleton
  fun provideGetSliderUseCase(
    homeRepository: HomeRepository
  ): GetSliderUseCase {
    return GetSliderUseCase(homeRepository)
  }
}
