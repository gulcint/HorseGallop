package com.horsegallop.data.home.repository

import com.horsegallop.domain.home.model.RideSession
import com.horsegallop.domain.home.model.UserStats
import com.horsegallop.domain.home.repository.HomeRepository
import com.horsegallop.data.remote.ApiService
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HomeRepositoryImpl @Inject constructor(
  private val api: ApiService
) : HomeRepository {

  override fun getRecentActivities(userId: String, limit: Int): Flow<Result<List<RideSession>>> = flow {
    emit(Result.success(emptyList()))
  }

  override fun getUserStats(userId: String): Flow<Result<UserStats>> = flow {
    emit(Result.success(UserStats(0, 0.0)))
  }
}
