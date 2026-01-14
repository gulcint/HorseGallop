package com.horsegallop.data.ride.repository

import com.horsegallop.domain.ride.model.GeoPoint
import com.horsegallop.domain.ride.model.RideMetrics
import com.horsegallop.domain.ride.repository.RideRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RideRepositoryImpl @Inject constructor() : RideRepository {

    private val _isRiding = MutableStateFlow(false)
    override val isRiding = _isRiding.asStateFlow()

    private val _rideMetrics = MutableStateFlow(RideMetrics(
        pathPoints = listOf(GeoPoint(41.0, 29.0))
    ))
    override val rideMetrics = _rideMetrics.asStateFlow()
    
    private var isAutoDetectEnabled = false

    private val scope = CoroutineScope(Dispatchers.Default)
    private var rideJob: Job? = null

    override suspend fun startRide() {
        if (_isRiding.value) return
        _isRiding.value = true
        startMockLoop()
    }

    override suspend fun stopRide() {
        _isRiding.value = false
        rideJob?.cancel()
        _rideMetrics.update { it.copy(speedKmh = 0f) }
    }

    override suspend fun setAutoDetect(enabled: Boolean) {
        isAutoDetectEnabled = enabled
    }

    private fun startMockLoop() {
        rideJob?.cancel()
        rideJob = scope.launch {
            while (isActive && _isRiding.value) {
                delay(1000)
                _rideMetrics.update { cur ->
                    val newDuration = cur.durationSec + 1
                    val newSpeed = ((10..22).random()) / 2f
                    val newDistance = cur.distanceKm + (newSpeed / 3600f)
                    val weightKg = 75f
                    val met = 5.5f
                    val kcal = (weightKg * (newDuration / 60f) * met / 60f).toInt()
                    
                    val last = cur.pathPoints.lastOrNull() ?: GeoPoint(41.0, 29.0)
                    val jitterLat = (listOf(-0.0005, -0.0003, 0.0, 0.0003, 0.0005)).random()
                    val jitterLng = (listOf(-0.0005, -0.0003, 0.0, 0.0003, 0.0005)).random()
                    val next = GeoPoint(last.latitude + jitterLat, last.longitude + jitterLng)
                    val updatedPath = (cur.pathPoints + next).takeLast(200)
                    
                    cur.copy(
                        speedKmh = newSpeed,
                        distanceKm = newDistance,
                        durationSec = newDuration,
                        calories = kcal,
                        pathPoints = updatedPath
                    )
                }
            }
        }
    }
}
