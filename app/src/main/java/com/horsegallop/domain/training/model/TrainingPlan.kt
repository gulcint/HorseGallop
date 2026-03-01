package com.horsegallop.domain.training.model

enum class TrainingPlanStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    LOCKED
}

data class TrainingPlan(
    val id: String,
    val title: String,
    val summary: String,
    val weeklyGoal: Int,
    val progressPercent: Int,
    val streakDays: Int,
    val status: TrainingPlanStatus,
    val tasks: List<TrainingTask>
)
