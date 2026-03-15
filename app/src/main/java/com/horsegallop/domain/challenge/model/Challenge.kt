package com.horsegallop.domain.challenge.model

enum class ChallengeType { MONTHLY_DISTANCE, WEEKLY_RIDES, SPEED_GOAL, EXPLORE_BARNS }

data class Challenge(
    val id: String,
    val type: ChallengeType,
    val title: String,
    val description: String,
    val targetValue: Double,
    val currentValue: Double,
    val unit: String,
    val startDate: Long,
    val endDate: Long,
    val isCompleted: Boolean = false,
    val reward: BadgeType? = null
) {
    val progress: Float get() = (currentValue / targetValue).toFloat().coerceIn(0f, 1f)
    val daysLeft: Int get() = ((endDate - System.currentTimeMillis()) / 86400_000L).toInt().coerceAtLeast(0)
}
