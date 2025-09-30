package com.example.data.di

import com.example.domain.repository.HomeRepository
import com.example.domain.usecase.GetSliderUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {
  
  @Provides
  @ViewModelScoped
  fun provideGetSliderUseCase(
    homeRepository: HomeRepository
  ): GetSliderUseCase {
    return GetSliderUseCase(homeRepository)
  }
}
