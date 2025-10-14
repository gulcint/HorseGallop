package com.horsegallop.feature.home.data.di

import com.horsegallop.feature.home.domain.repository.HomeRepository
import com.horsegallop.feature.home.domain.usecase.GetSliderUseCase
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
