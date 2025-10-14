package com.horsegallop.feature.home.domain.repository

import com.horsegallop.feature.home.domain.model.SliderItem
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
  fun getSlider(): Flow<Result<List<SliderItem>>>
}
