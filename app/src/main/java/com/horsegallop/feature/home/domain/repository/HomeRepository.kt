package com.horsegallop.feature.home.domain.repository

import com.horsegallop.feature.home.domain.model.RideSession
import com.horsegallop.feature.home.domain.model.UserStats
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
  fun getRecentActivities(userId: String, limit: Int = 5): Flow<Result<List<RideSession>>>
  fun getUserStats(userId: String): Flow<Result<UserStats>>
}
