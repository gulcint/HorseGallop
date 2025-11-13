package com.horsegallop.data.mock

import com.horsegallop.domain.model.RiderProfile
import com.horsegallop.domain.model.TrainingType

object FakeRiderData {
  val riders: List<RiderProfile> = listOf(
    RiderProfile(id = "R-001", fullName = "Gulcin Tas", experienceYears = 5, preferredTrainingType = TrainingType.TRAIL_RIDING),
    RiderProfile(id = "R-002", fullName = "Ada Demir", experienceYears = 3, preferredTrainingType = TrainingType.DRESSAGE)
  )
}


