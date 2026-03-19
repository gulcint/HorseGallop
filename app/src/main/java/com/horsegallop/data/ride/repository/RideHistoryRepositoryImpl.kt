package com.horsegallop.data.ride.repository

import android.content.Context
import com.horsegallop.core.debug.AppLog
import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.data.remote.supabase.SupabaseRideDto
import com.horsegallop.data.remote.supabase.SupabaseRidePathPointDto
import com.horsegallop.domain.ride.model.GeoPoint
import com.horsegallop.domain.ride.model.RideSession
import com.horsegallop.domain.ride.repository.RideHistoryRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RideHistoryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supabaseDataSource: SupabaseDataSource
) : RideHistoryRepository {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, RideSession::class.java)
    private val adapter = moshi.adapter<List<RideSession>>(listType)
    private val file by lazy { File(context.filesDir, "ride_history.json") }

    private val _history = MutableStateFlow<List<RideSession>>(emptyList())

    init {
        CoroutineScope(Dispatchers.IO).launch {
            if (file.exists()) {
                try {
                    val json = file.readText()
                    val list = adapter.fromJson(json) ?: emptyList()
                    _history.value = list
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            try {
                val remote = supabaseDataSource.getMyRides().map { dto ->
                    val dateMillis = parseIsoToMillis(dto.startedAt)
                    RideSession(
                        id = dto.id,
                        dateMillis = dateMillis,
                        durationSec = dto.durationSec,
                        distanceKm = dto.distanceKm.toFloat(),
                        calories = dto.calories.toInt(),
                        pathPoints = emptyList(),
                        barnName = dto.barnName,
                        avgSpeedKmh = dto.avgSpeedKmh.toFloat(),
                        maxSpeedKmh = dto.maxSpeedKmh.toFloat(),
                        rideType = dto.rideType
                    )
                }
                _history.value = remote
            } catch (e: Exception) {
                // Keep local cache on failure
                e.printStackTrace()
            }
        }
    }

    override fun getRideHistory(): Flow<List<RideSession>> = _history

    override fun getRide(id: String): Flow<RideSession?> = _history.map { list ->
        list.find { it.id == id }
    }

    override suspend fun saveRide(ride: RideSession) {
        // Write to local cache first (offline-first)
        val updatedList = listOf(ride) + _history.value
        _history.value = updatedList
        withContext(Dispatchers.IO) {
            try {
                val json = adapter.toJson(updatedList)
                file.writeText(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Sync to Supabase asynchronously
            try {
                val uid = supabaseDataSource.currentUserId() ?: return@withContext
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                val rideDto = SupabaseRideDto(
                    id = ride.id,
                    userId = uid,
                    durationSec = ride.durationSec,
                    distanceKm = ride.distanceKm.toDouble(),
                    calories = ride.calories.toDouble(),
                    avgSpeedKmh = ride.avgSpeedKmh.toDouble(),
                    maxSpeedKmh = ride.maxSpeedKmh.toDouble(),
                    rideType = ride.rideType ?: "FREE",
                    barnName = ride.barnName,
                    startedAt = isoFormat.format(java.util.Date(ride.dateMillis)),
                    savedAt = isoFormat.format(java.util.Date())
                )
                val pathDtos = ride.pathPoints.mapIndexed { idx, geoPoint ->
                    SupabaseRidePathPointDto(
                        rideId = ride.id,
                        userId = uid,
                        lat = geoPoint.latitude,
                        lng = geoPoint.longitude,
                        altM = geoPoint.altitudeM.toDouble(),
                        speedKmh = geoPoint.speedKmh.toDouble(),
                        sortOrder = idx
                    )
                }
                supabaseDataSource.saveRide(rideDto, pathDtos)
            } catch (e: Exception) {
                AppLog.e("RideHistoryRepo", "Supabase sync failed: ${e.message}")
            }
        }
    }

    private fun parseIsoToMillis(iso: String): Long = runCatching {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(iso)?.time
            ?: SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(iso)?.time
            ?: 0L
    }.getOrDefault(0L)
}
