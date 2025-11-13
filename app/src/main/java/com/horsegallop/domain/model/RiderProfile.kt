package com.horsegallop.domain.model

data class RiderProfile(
  val id: String,
  val fullName: String,
  val experienceYears: Int,
  val preferredTrainingType: TrainingType
)


