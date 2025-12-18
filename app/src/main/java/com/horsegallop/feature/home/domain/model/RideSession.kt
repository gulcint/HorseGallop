package com.horsegallop.feature.home.domain.model

import java.util.Date

data class RideSession(
    val id: String,
    val title: String,
    val timestamp: Date?,
    val durationMin: Int,
    val distanceKm: Double
)
