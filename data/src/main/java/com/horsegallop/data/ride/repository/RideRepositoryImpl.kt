package com.horsegallop.data.ride.repository

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.horsegallop.domain.ride.model.GeoPoint
import com.horsegallop.domain.ride.model.RideMetrics
import com.horsegallop.domain.ride.model.RideSession
import com.horsegallop.domain.ride.repository.RideHistoryRepository
import com.horsegallop.domain.ride.repository.RideRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Singleton
class RideRepositoryImpl @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val rideHistoryRepository: RideHistoryRepository
) : RideRepository {

    private val _isRiding = MutableStateFlow(false)
    override val isRiding = _isRiding.asStateFlow()

    private val _rideMetrics = MutableStateFlow(
        RideMetrics(
            pathPoints = emptyList()
        )
    )
    override val rideMetrics = _rideMetrics.asStateFlow()

    private var isAutoDetectEnabled = false

    private var locationCallback: LocationCallback? = null
    private var lastLocationTimeMillis: Long? = null
    private var accumulatedCalories: Double = 0.0
    private var startTimeMillis: Long = 0L
    private var userWeightKg: Float = 70f

    override suspend fun startRide(weightKg: Float) {
        if (_isRiding.value) return
        _isRiding.value = true
        userWeightKg = weightKg
        startTimeMillis = System.currentTimeMillis()
        lastLocationTimeMillis = null
        accumulatedCalories = 0.0
        // Clear previous path
        _rideMetrics.value = RideMetrics(pathPoints = emptyList())
        startLocationUpdates()
    }

    override suspend fun stopRide(barnName: String?) {
        if (_isRiding.value) {
            val metrics = _rideMetrics.value
            // Save ride session
            if (metrics.pathPoints.isNotEmpty()) {
                val session = RideSession(
                    id = UUID.randomUUID().toString(),
                    dateMillis = startTimeMillis,
                    durationSec = metrics.durationSec,
                    distanceKm = metrics.distanceKm,
                    calories = metrics.calories,
                    pathPoints = metrics.pathPoints,
                    barnName = barnName
                )
                rideHistoryRepository.saveRide(session)
            }
        }
        _isRiding.value = false
        stopLocationUpdates()
        _rideMetrics.update { it.copy(speedKmh = 0f) }
    }

    override suspend fun setAutoDetect(enabled: Boolean) {
        isAutoDetectEnabled = enabled
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L
        )
            .setMinUpdateIntervalMillis(1000L)
            .setWaitForAccurateLocation(true)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val locations = result.locations
                if (locations.isEmpty()) return
                val location = locations.last()
                
                // Filter out (0,0) points common in emulators
                if (location.latitude == 0.0 && location.longitude == 0.0) return
                
                val now = System.currentTimeMillis()
                val previousTime = lastLocationTimeMillis
                lastLocationTimeMillis = now

                val lat = location.latitude
                val lng = location.longitude
                val newPoint = GeoPoint(lat, lng)

                _rideMetrics.update { cur ->
                    val lastPoint = cur.pathPoints.lastOrNull()
                    val distanceDeltaKm = if (lastPoint != null) {
                        distanceKm(
                            lastPoint.latitude,
                            lastPoint.longitude,
                            newPoint.latitude,
                            newPoint.longitude
                        )
                    } else {
                        0.0
                    }

                    val secondsDelta = if (previousTime != null) {
                        val diff = ((now - previousTime) / 1000L).toInt()
                        if (diff <= 0) 1 else diff
                    } else {
                        0
                    }

                    val newDuration = cur.durationSec + secondsDelta
                    val newDistance = cur.distanceKm + distanceDeltaKm.toFloat()

                    // Use location.speed if available, otherwise calculate
                    val currentSpeedKmh = if (location.hasSpeed()) {
                        location.speed * 3.6f
                    } else if (secondsDelta > 0 && distanceDeltaKm > 0.0) {
                        ((distanceDeltaKm / secondsDelta.toDouble()) * 3600.0).toFloat()
                    } else {
                        0f
                    }

                    // Dynamic MET Calculation based on Speed
                    // Walk: < 6 km/h (~3.8 MET)
                    // Trot: 6 - 13 km/h (~5.5 MET)
                    // Canter/Gallop: > 13 km/h (~7.3 MET)
                    val currentMet = when {
                        currentSpeedKmh < 6 -> 3.8f
                        currentSpeedKmh < 13 -> 5.5f
                        else -> 7.3f
                    }
                    
                    val weightKg = userWeightKg
                    
                    // Calories = MET * Weight * Duration(hours)
                    // We calculate incrementally for this segment
                    if (secondsDelta > 0) {
                        val hoursDelta = secondsDelta / 3600.0
                        val caloriesDelta = currentMet * weightKg * hoursDelta
                        accumulatedCalories += caloriesDelta
                    }

                    val updatedPath = (cur.pathPoints + newPoint) // Keep all points for now or optimize later

                    cur.copy(
                        speedKmh = currentSpeedKmh,
                        distanceKm = newDistance,
                        durationSec = newDuration,
                        calories = accumulatedCalories.toInt(),
                        pathPoints = updatedPath
                    )
                }
            }
        }

        locationCallback = callback
        fusedLocationClient.requestLocationUpdates(
            request,
            callback,
            android.os.Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        val callback = locationCallback ?: return
        fusedLocationClient.removeLocationUpdates(callback)
        locationCallback = null
        lastLocationTimeMillis = null
    }

    private fun distanceKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val radius = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(rLat1) * cos(rLat2) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return radius * c
    }
}
