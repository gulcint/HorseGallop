package com.horsegallop.feature.home.domain.usecase

import com.horsegallop.feature.home.domain.model.SliderItem
import com.horsegallop.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow

class GetSliderUseCase(
  private val repository: HomeRepository
) {
  operator fun invoke(): Flow<Result<List<SliderItem>>> {
    return repository.getSlider()
  }
}
