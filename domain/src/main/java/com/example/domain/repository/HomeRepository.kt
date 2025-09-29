package com.example.domain.repository

import com.example.domain.model.SliderItem
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
  fun getSlider(): Flow<Result<List<SliderItem>>>
}
