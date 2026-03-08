package com.horsegallop.domain.schedule.model

data class Lesson(
    val id: String,
    val date: String,
    val title: String,
    val instructorName: String,
    val durationMin: Int = 0,
    val level: String = "",
    val price: Double = 0.0,
    val spotsTotal: Int = 0,
    val spotsAvailable: Int = 0,
    val isBookedByMe: Boolean = false
) {
    val isFull: Boolean get() = spotsAvailable <= 0
}
