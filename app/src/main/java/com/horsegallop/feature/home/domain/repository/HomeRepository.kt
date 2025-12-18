package com.horsegallop.feature.home.domain.repository

import com.horsegallop.feature.home.domain.model.RideSession
import com.horsegallop.feature.home.domain.model.SliderItem
import com.horsegallop.feature.home.domain.model.UserStats
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
  fun getSlider(): Flow<Result<List<SliderItem>>>
  fun getRecentActivities(userId: String): Flow<Result<List<RideSession>>>
  fun getUserStats(userId: String): Flow<Result<UserStats>>
}
