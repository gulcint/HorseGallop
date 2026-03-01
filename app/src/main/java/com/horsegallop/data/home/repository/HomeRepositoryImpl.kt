package com.horsegallop.data.home.repository

import com.horsegallop.data.remote.functions.AppFunctionsDataSource
import com.horsegallop.domain.home.model.RideSession
import com.horsegallop.domain.home.model.UserStats
import com.horsegallop.domain.home.repository.HomeRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HomeRepositoryImpl @Inject constructor(
  private val functionsDataSource: AppFunctionsDataSource
) : HomeRepository {

  override fun getRecentActivities(userId: String, limit: Int): Flow<Result<List<RideSession>>> = flow {
    try {
      val dashboard = functionsDataSource.getHomeDashboard(limit)
      val mapped = dashboard.recentActivities.map { activity ->
        RideSession(
          id = activity.id,
          title = activity.title,
          timestamp = parseDate(activity.dateLabel, activity.timeLabel),
          durationMin = activity.durationMin,
          distanceKm = activity.distanceKm
        )
      }
      emit(Result.success(mapped))
    } catch (e: Exception) {
      emit(Result.failure(e))
    }
  }

  override fun getUserStats(userId: String): Flow<Result<UserStats>> = flow {
    try {
      val dashboard = functionsDataSource.getHomeDashboard(limit = 20)
      val stats = dashboard.stats
      emit(
        Result.success(
          UserStats(
            totalRides = stats.totalRides,
            totalDistance = stats.totalDistanceKm,
            totalDurationMin = stats.totalDurationMin,
            totalCalories = stats.totalCalories,
            favoriteBarn = stats.favoriteBarn,
            lastRideAt = dashboard.recentActivities.firstOrNull()?.let {
              parseDate(it.dateLabel, it.timeLabel)
            }
          )
        )
      )
    } catch (e: Exception) {
      emit(Result.failure(e))
    }
  }

  private fun parseDate(dateLabel: String, timeLabel: String): Date? {
    val value = "$dateLabel ${if (timeLabel.isBlank()) "00:00" else timeLabel}".trim()
    return runCatching {
      SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse(value)
    }.getOrNull()
  }
}
