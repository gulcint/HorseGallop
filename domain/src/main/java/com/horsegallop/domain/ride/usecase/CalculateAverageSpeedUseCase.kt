package com.horsegallop.domain.ride.usecase

import com.horsegallop.domain.ride.model.RideSession
import javax.inject.Inject

class CalculateAverageSpeedUseCase @Inject constructor() {
    operator fun invoke(rides: List<RideSession>): Float {
        if (rides.isEmpty()) return 0f
        val totalSpeed = rides.filter { it.avgSpeedKmh > 0 }.sumOf { it.avgSpeedKmh.toDouble() }
        return if (rides.filter { it.avgSpeedKmh > 0 }.size > 0) {
            (totalSpeed / rides.filter { it.avgSpeedKmh > 0 }.size).toFloat()
        } else {
            0f
        }
    }
}
