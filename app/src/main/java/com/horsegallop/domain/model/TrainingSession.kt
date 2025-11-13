package com.horsegallop.domain.model

import java.time.Instant

data class TrainingSession(
  val id: String,
  val horseId: String,
  val riderId: String,
  val type: TrainingType,
  val status: TrainingStatus,
  val startTime: Instant,
  val durationMin: Int,
  val distanceKm: Double,
  val averageSpeedKmh: Double,
  val notes: String?
)


