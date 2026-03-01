package com.horsegallop.domain.training.model

enum class TrainingTaskStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    LOCKED
}

data class TrainingTask(
    val id: String,
    val title: String,
    val description: String,
    val targetMinutes: Int,
    val status: TrainingTaskStatus
)
