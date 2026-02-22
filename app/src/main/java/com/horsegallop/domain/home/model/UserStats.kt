package com.horsegallop.domain.home.model

import java.util.Date

data class UserStats(
    val totalRides: Int,
    val totalDistance: Double,
    val lastRideAt: Date? = null,
    val totalDurationMin: Int = 0,
    val totalCalories: Double = 0.0,
    val favoriteBarn: String? = null
)
