package com.horsegallop.domain.ride.usecase

import com.horsegallop.domain.ride.model.RideSession
import javax.inject.Inject

class CalculateAverageCaloriesUseCase @Inject constructor() {
    operator fun invoke(rides: List<RideSession>): Float {
        if (rides.isEmpty()) return 0f
        val totalCalories = rides.sumOf { it.calories.toDouble() }
        return (totalCalories / rides.size).toFloat()
    }
}
