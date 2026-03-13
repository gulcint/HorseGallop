package com.horsegallop.data.ride.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import androidx.core.content.ContextCompat
import com.horsegallop.core.debug.AppLog
import com.horsegallop.core.util.haversineKm
import com.horsegallop.data.remote.dto.GeoPointDto
import com.horsegallop.data.remote.dto.StartRideRequestDto
import com.horsegallop.data.remote.dto.StopRideRequestDto
import com.horsegallop.domain.ride.model.GeoPoint
import com.horsegallop.domain.ride.model.RideMetrics
import com.horsegallop.domain.ride.model.RideSession
import com.horsegallop.domain.ride.model.StopRideResult
import com.horsegallop.domain.ride.repository.RideHistoryRepository
import com.horsegallop.domain.ride.repository.RideRepository
import com.horsegallop.domain.ride.util.GaitThresholds
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val rideHistoryRepository: RideHistoryRepository,
    private val apiService: com.horsegallop.data.remote.ApiService,
    private val stopSyncOrchestrator: RideStopSyncOrchestrator
) : RideRepository {

    private val _isRiding = MutableStateFlow(false)
    override val isRiding = _isRiding.asStateFlow()

    private val _rideMetrics = MutableStateFlow(
        RideMetrics(
            pathPoints = emptyList()
        )
    )
    override val rideMetrics = _rideMetrics.asStateFlow()
    override val pendingSyncCount: Flow<Int> = stopSyncOrchestrator.pendingSyncCount

    private val _autoStopSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val autoStopSignal: Flow<Unit> = _autoStopSignal.asSharedFlow()

    private var isAutoDetectEnabled = false
    private var stillnessStartMillis: Long? = null

    private var locationCallback: LocationCallback? = null
    private var lastLocationTimeMillis: Long? = null
    private var accumulatedCalories: Double = 0.0
    private var accumulatedHorseCalories: Double = 0.0
    private var startTimeMillis: Long = 0L
    private var userWeightKg: Float = 70f
    private var currentRideId: String? = null
    private var speedSamples: Int = 0
    private var speedSum: Double = 0.0
    private var maxSpeed: Double = 0.0
    private var currentRideType: String? = null
    private val speedWindow = ArrayDeque<Float>(5)

    override suspend fun startRide(weightKg: Float, rideType: String?) {
        if (_isRiding.value) return
        if (!hasLocationPermission()) {
            AppLog.e("RideRepositoryImpl", "Cannot start ride without location permission")
            _isRiding.value = false
            return
        }
        _isRiding.value = true
        userWeightKg = weightKg
        currentRideType = normalizeRideType(rideType)
        startTimeMillis = System.currentTimeMillis()
        lastLocationTimeMillis = null
        accumulatedCalories = 0.0
        accumulatedHorseCalories = 0.0
        speedSamples = 0
        speedSum = 0.0
        maxSpeed = 0.0
        speedWindow.clear()
        stillnessStartMillis = null
        // Clear previous path
        _rideMetrics.value = RideMetrics(pathPoints = emptyList())
        currentRideId = try {
            apiService.startRide(
                StartRideRequestDto(
                    rideType = currentRideType,
                    startLocation = null
                )
            ).id
        } catch (e: Exception) {
            null
        }
        startLocationUpdates()
    }

    override suspend fun stopRide(barnName: String?): StopRideResult {
        var localSaved = false
        var remoteSynced = false
        var pendingSyncId: String? = null
        if (_isRiding.value) {
            val metrics = _rideMetrics.value
            // Save ride session
            if (metrics.pathPoints.isNotEmpty()) {
                val avgSpeedKmh = if (speedSamples > 0) speedSum / speedSamples else 0.0
                val downsampledPath = downsamplePath(metrics.pathPoints, 1500)
                val session = RideSession(
                    id = UUID.randomUUID().toString(),
                    dateMillis = startTimeMillis,
                    durationSec = metrics.durationSec,
                    distanceKm = metrics.distanceKm,
                    calories = metrics.calories,
                    pathPoints = downsampledPath,
                    barnName = barnName,
                    avgSpeedKmh = avgSpeedKmh.toFloat(),
                    maxSpeedKmh = maxSpeed.toFloat(),
                    rideType = currentRideType
                )
                rideHistoryRepository.saveRide(session)
                localSaved = true
            }
            val rideId = currentRideId
            val avgSpeedKmh = if (speedSamples > 0) speedSum / speedSamples else 0.0
            val durationMin = metrics.durationSec / 60.0
            val points = downsamplePath(metrics.pathPoints, 500).map {
                GeoPointDto(it.latitude, it.longitude, null)
            }
            val stopRequest = StopRideRequestDto(
                distanceKm = metrics.distanceKm.toDouble(),
                durationMin = durationMin,
                calories = metrics.calories.toDouble(),
                avgSpeedKmh = avgSpeedKmh,
                maxSpeedKmh = maxSpeed,
                pathPoints = points
            )
            val syncResult = stopSyncOrchestrator.syncStopOrQueue(
                rideId = rideId,
                request = stopRequest,
                stopRemote = { id, body ->
                    apiService.stopRide(id, body)
                }
            )
            remoteSynced = syncResult.remoteSynced
            pendingSyncId = syncResult.pendingSyncId
        }
        _isRiding.value = false
        stopLocationUpdates()
        speedWindow.clear()
        _rideMetrics.update { it.copy(speedKmh = 0f) }
        currentRideId = null
        currentRideType = null
        return StopRideResult(
            localSaved = localSaved,
            remoteSynced = remoteSynced,
            pendingSyncId = pendingSyncId
        )
    }

    override suspend fun retryPendingRideSync() {
        stopSyncOrchestrator.retryDuePendingSync { id, body ->
            apiService.stopRide(id, body)
        }
    }

    override suspend fun setAutoDetect(enabled: Boolean) {
        isAutoDetectEnabled = enabled
    }

    private fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            _isRiding.value = false
            AppLog.e("RideRepositoryImpl", "Location permission missing before requesting updates")
            return
        }
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
                val altM = location.altitude.toFloat()

                _rideMetrics.update { cur ->
                    val lastPoint = cur.pathPoints.lastOrNull()
                    val distanceDeltaKm = if (lastPoint != null) {
                        haversineKm(
                            lastPoint.latitude,
                            lastPoint.longitude,
                            lat,
                            lng
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

                    // Raw speed from GPS sensor or distance/time calculation
                    val rawSpeedKmh = if (location.hasSpeed()) {
                        location.speed * 3.6f
                    } else if (secondsDelta > 0 && distanceDeltaKm > 0.0) {
                        ((distanceDeltaKm / secondsDelta.toDouble()) * 3600.0).toFloat()
                    } else {
                        0f
                    }

                    // 5-point moving average to smooth GPS jitter
                    speedWindow.addLast(rawSpeedKmh)
                    if (speedWindow.size > 5) speedWindow.removeFirst()
                    val smoothedSpeed = speedWindow.average().toFloat()

                    if (smoothedSpeed > 0f) {
                        speedSamples += 1
                        speedSum += smoothedSpeed
                        if (smoothedSpeed > maxSpeed) maxSpeed = smoothedSpeed.toDouble()
                    }

                    // Dynamic MET based on smoothed speed
                    val currentMet = when {
                        smoothedSpeed < GaitThresholds.WALK_MAX_KMH -> GaitThresholds.WALK_MET
                        smoothedSpeed < GaitThresholds.TROT_MAX_KMH -> GaitThresholds.TROT_MET
                        else -> GaitThresholds.CANTER_MET
                    }

                    // Horse calorie estimate: avg 500 kg horse, speed-tier kcal/hr
                    val horseKcalPerHour = when {
                        smoothedSpeed < GaitThresholds.WALK_MAX_KMH -> GaitThresholds.WALK_HORSE_KCAL_HR
                        smoothedSpeed < GaitThresholds.TROT_MAX_KMH -> GaitThresholds.TROT_HORSE_KCAL_HR
                        else -> GaitThresholds.CANTER_HORSE_KCAL_HR
                    }

                    if (secondsDelta > 0) {
                        val hoursDelta = secondsDelta / 3600.0
                        accumulatedCalories += currentMet * userWeightKg * hoursDelta
                        accumulatedHorseCalories += horseKcalPerHour * hoursDelta
                    }

                    // Auto-stop: 5 min stillness detection (speed below walk threshold)
                    if (isAutoDetectEnabled) {
                        if (smoothedSpeed < GaitThresholds.WALK_MAX_KMH * 0.3f) {
                            if (stillnessStartMillis == null) {
                                stillnessStartMillis = now
                            } else if (now - stillnessStartMillis!! >= 5 * 60 * 1000L) {
                                _autoStopSignal.tryEmit(Unit)
                                stillnessStartMillis = null
                            }
                        } else {
                            stillnessStartMillis = null
                        }
                    }

                    val newPoint = GeoPoint(lat, lng, smoothedSpeed, altM)
                    val updatedPath = cur.pathPoints + newPoint

                    cur.copy(
                        speedKmh = smoothedSpeed,
                        distanceKm = newDistance,
                        durationSec = newDuration,
                        calories = accumulatedCalories.toInt(),
                        horseCalories = accumulatedHorseCalories.toInt(),
                        altitudeM = altM,
                        pathPoints = updatedPath
                    )
                }
            }
        }

        locationCallback = callback
        try {
            fusedLocationClient.requestLocationUpdates(
                request,
                callback,
                android.os.Looper.getMainLooper()
            )
        } catch (securityException: SecurityException) {
            // Runtime permission can be revoked after ride starts; fail safely.
            AppLog.e("RideRepositoryImpl", "Location permission missing: ${securityException.message}")
            _isRiding.value = false
            locationCallback = null
            lastLocationTimeMillis = null
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    private fun normalizeRideType(raw: String?): String? {
        val normalized = raw?.trim()?.lowercase() ?: return null
        return normalized.takeIf { it in setOf("dressage", "show_jumping", "endurance", "trail_riding") }
    }

    private fun stopLocationUpdates() {
        val callback = locationCallback ?: return
        fusedLocationClient.removeLocationUpdates(callback)
        locationCallback = null
        lastLocationTimeMillis = null
    }

    private fun downsamplePath(points: List<GeoPoint>, maxPoints: Int): List<GeoPoint> {
        if (points.size <= maxPoints) return points
        val step = (points.size.toDouble() / maxPoints).coerceAtLeast(1.0)
        val result = ArrayList<GeoPoint>(maxPoints)
        var idx = 0.0
        while (idx < points.size) {
            result.add(points[idx.toInt()])
            idx += step
        }
        return result
    }
}
