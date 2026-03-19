package com.horsegallop.data.home.repository

import com.horsegallop.data.remote.supabase.SupabaseDataSource
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
    private val supabaseDataSource: SupabaseDataSource
) : HomeRepository {

    override fun getRecentActivities(userId: String, limit: Int): Flow<Result<List<RideSession>>> = flow {
        try {
            val rides = supabaseDataSource.getRecentRides(limit)
            val mapped = rides.map { dto ->
                RideSession(
                    id = dto.id,
                    title = dto.barnName,
                    timestamp = parseIso(dto.startedAt),
                    durationMin = dto.durationSec / 60,
                    distanceKm = dto.distanceKm
                )
            }
            emit(Result.success(mapped))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getUserStats(userId: String): Flow<Result<UserStats>> = flow {
        try {
            val rides = supabaseDataSource.getMyRides()
            val totalRides = rides.size
            val totalDistance = rides.sumOf { it.distanceKm }
            val totalDuration = rides.sumOf { it.durationSec }
            val totalCalories = rides.sumOf { it.calories }
            val favoriteBarn = rides.mapNotNull { it.barnName }
                .groupingBy { it }.eachCount()
                .maxByOrNull { it.value }?.key
            val lastRideAt = rides.maxByOrNull { it.startedAt }?.let { parseIso(it.startedAt) }
            emit(
                Result.success(
                    UserStats(
                        totalRides = totalRides,
                        totalDistance = totalDistance,
                        totalDurationMin = totalDuration / 60,
                        totalCalories = totalCalories,
                        favoriteBarn = favoriteBarn,
                        lastRideAt = lastRideAt
                    )
                )
            )
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    private fun parseIso(iso: String): Date? = runCatching {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(iso)
            ?: SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(iso)
    }.getOrNull()
}
