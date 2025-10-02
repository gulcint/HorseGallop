package com.horsegallop.domain.repository

import com.horsegallop.domain.model.SliderItem
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
  fun getSlider(): Flow<Result<List<SliderItem>>>
}
