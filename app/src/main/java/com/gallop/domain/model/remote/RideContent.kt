package com.horsegallop.domain.model.content

data class RideStatLabels(
  val totalDistanceLabel: String?,
  val totalTimeLabel: String?,
  val avgSpeedLabel: String?,
  val thisWeekLabel: String?
)

data class RideContent(
  val headline: String,
  val weatherCallout: String?,
  val startRideText: String,
  val statLabels: RideStatLabels?
)



