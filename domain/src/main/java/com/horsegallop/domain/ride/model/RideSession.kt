package com.horsegallop.domain.ride.model

import kotlin.math.round

data class RideSession(
    val id: String,
    val dateMillis: Long,
    val durationSec: Int,
    val distanceKm: Float,
    val calories: Int,
    val pathPoints: List<GeoPoint>,
    val barnName: String? = null
) {
    // Average speed in km/h
    val avgSpeedKmh: Float
        get() = if (durationSec > 0) {
            round((distanceKm / (durationSec / 3600f)) * 10f) / 10f
        } else {
            0f
        }
    
    // Calories per hour
    val caloriesPerHour: Float
        get() = if (durationSec > 0) {
            round((calories / (durationSec / 3600f)) * 10f) / 10f
        } else {
            0f
        }
    
    // Average pace in min/km
    val averagePaceMinPerKm: Float
        get() = if (distanceKm > 0) {
            round(((durationSec / 60f) / distanceKm) * 100f) / 100f
        } else {
            0f
        }
    
    // Estimated calories burned based on MET values
    // Walk: < 6 km/h (~3.8 MET)
    // Trot: 6 - 13 km/h (~5.5 MET)
    // Canter/Gallop: > 13 km/h (~7.3 MET)
    val estimatedMET: Float
        get() = when {
            avgSpeedKmh < 6 -> 3.8f
            avgSpeedKmh < 13 -> 5.5f
            else -> 7.3f
        }
    
    // Format duration as HH:MM:SS
    val formattedDuration: String
        get() = formatDuration(durationSec)
    
    // Format distance with 2 decimal places
    val formattedDistance: String
        get() = "%.2f".format(distanceKm)
    
    // Format speed with 1 decimal place
    val formattedSpeed: String
        get() = "%.1f".format(avgSpeedKmh)
    
    companion object {
        private fun formatDuration(seconds: Int): String {
            val h = seconds / 3600
            val m = (seconds % 3600) / 60
            val s = seconds % 60
            return if (h > 0) {
                "%02d:%02d:%02d".format(h, m, s)
            } else {
                "%02d:%02d".format(m, s)
            }
        }
        
        // Create a mock ride session for testing
        fun createMock(): RideSession {
            return RideSession(
                id = "mock_ride_${System.currentTimeMillis()}",
                dateMillis = System.currentTimeMillis(),
                durationSec = 2700, // 45 minutes
                distanceKm = 8.5f,
                calories = 420,
                pathPoints = emptyList(),
                barnName = "Downtown Barn"
            )
        }
    }
}
