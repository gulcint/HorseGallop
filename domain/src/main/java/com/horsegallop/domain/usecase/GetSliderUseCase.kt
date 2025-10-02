package com.horsegallop.domain.usecase

import com.horsegallop.domain.model.SliderItem
import com.horsegallop.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow

class GetSliderUseCase(
  private val repository: HomeRepository
) {
  operator fun invoke(): Flow<Result<List<SliderItem>>> {
    return repository.getSlider()
  }
}
