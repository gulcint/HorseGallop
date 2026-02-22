package com.horsegallop.data.home.repository

import com.horsegallop.domain.home.model.RideSession
import com.horsegallop.domain.home.model.UserStats
import com.horsegallop.domain.home.repository.HomeRepository
import com.horsegallop.data.remote.ApiService
import com.horsegallop.domain.ride.repository.RideHistoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HomeRepositoryImpl @Inject constructor(
  private val api: ApiService,
  private val rideHistoryRepository: RideHistoryRepository
) : HomeRepository {

  override fun getRecentActivities(userId: String, limit: Int): Flow<Result<List<RideSession>>> {
    return rideHistoryRepository.getRideHistory().map { history ->
        val recent = history.sortedByDescending { it.dateMillis }.take(limit)
        val mapped = recent.map { ride ->
            RideSession(
                id = ride.id,
                title = ride.barnName,
                timestamp = java.util.Date(ride.dateMillis),
                durationMin = ride.durationSec / 60,
                distanceKm = ride.distanceKm.toDouble()
            )
        }
        Result.success(mapped)
    }
  }

  override fun getUserStats(userId: String): Flow<Result<UserStats>> {
    return rideHistoryRepository.getRideHistory().map { history ->
        val totalRides = history.size
        val totalDistance = history.sumOf { it.distanceKm.toDouble() }
        val lastRide = history.maxByOrNull { it.dateMillis }?.let { java.util.Date(it.dateMillis) }
        val totalDurationMin = history.sumOf { it.durationSec / 60 }
        val totalCalories = history.sumOf { it.calories.toDouble() }
        val favoriteBarn = history.mapNotNull { it.barnName }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

        Result.success(UserStats(
            totalRides = totalRides,
            totalDistance = totalDistance,
            lastRideAt = lastRide,
            totalDurationMin = totalDurationMin,
            totalCalories = totalCalories,
            favoriteBarn = favoriteBarn
        ))
    }
  }
}
