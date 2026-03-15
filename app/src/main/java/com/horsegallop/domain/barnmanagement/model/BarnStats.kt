package com.horsegallop.domain.barnmanagement.model

data class BarnStats(
    val totalLessons: Int,
    val totalReservations: Int,
    val uniqueStudents: Int,
    val upcomingLessonsCount: Int
)
