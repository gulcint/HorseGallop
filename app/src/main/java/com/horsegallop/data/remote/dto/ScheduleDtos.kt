package com.horsegallop.data.remote.dto

data class LessonDto(
    val id: String,
    val title: String,
    val instructorName: String,
    val barnName: String,
    val startTime: String, // ISO 8601
    val endTime: String, // ISO 8601
    val status: String // e.g., "confirmed", "pending", "cancelled"
)
