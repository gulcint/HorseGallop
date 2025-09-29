package com.example.domain.usecase

import com.example.domain.model.SliderItem
import com.example.domain.repository.HomeRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetSliderUseCase @Inject constructor(
  private val repository: HomeRepository
) {
  operator fun invoke(): Flow<Result<List<SliderItem>>> {
    return repository.getSlider()
  }
}
