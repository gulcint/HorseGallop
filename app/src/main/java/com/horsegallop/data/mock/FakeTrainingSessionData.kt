package com.horsegallop.data.mock

import com.horsegallop.domain.model.TrainingSession
import com.horsegallop.domain.model.TrainingStatus
import com.horsegallop.domain.model.TrainingType
import java.time.Instant

object FakeTrainingSessionData {
  val sessions: List<TrainingSession> = listOf(
    TrainingSession(
      id = "TS-001",
      horseId = "H-001",
      riderId = "R-001",
      type = TrainingType.TRAIL_RIDING,
      status = TrainingStatus.COMPLETED,
      startTime = Instant.now().minusSeconds(60L * 60L * 24L),
      durationMin = 45,
      distanceKm = 9.2,
      averageSpeedKmh = 12.3,
      notes = "Forest trail with light wind."
    ),
    TrainingSession(
      id = "TS-002",
      horseId = "H-002",
      riderId = "R-002",
      type = TrainingType.DRESSAGE,
      status = TrainingStatus.SCHEDULED,
      startTime = Instant.now().plusSeconds(60L * 60L * 6L),
      durationMin = 30,
      distanceKm = 0.0,
      averageSpeedKmh = 0.0,
      notes = null
    )
  )
}


