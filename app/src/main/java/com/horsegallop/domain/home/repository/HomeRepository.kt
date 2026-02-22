package com.horsegallop.domain.home.repository

import com.horsegallop.domain.home.model.RideSession
import com.horsegallop.domain.home.model.UserStats
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
  fun getRecentActivities(userId: String, limit: Int = 5): Flow<Result<List<RideSession>>>
  fun getUserStats(userId: String): Flow<Result<UserStats>>
}
