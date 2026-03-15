package com.horsegallop.domain.barnmanagement.model

data class ManagedLesson(
    val id: String,
    val title: String,
    val instructorName: String,
    val startTimeMs: Long,
    val durationMin: Int,
    val level: String,
    val price: Double,
    val spotsTotal: Int,
    val spotsBooked: Int,
    val barnId: String,
    val isCancelled: Boolean = false
)
