package com.horsegallop.domain.challenge.model

enum class BadgeType {
    FIRST_RIDE,
    DISTANCE_10K,
    DISTANCE_50K,
    DISTANCE_100K,
    STREAK_7,
    STREAK_30,
    SPEED_DEMON,
    EARLY_BIRD,
    MONTHLY_CHAMPION,
    SOCIAL_RIDER
}

data class Badge(
    val id: String,
    val type: BadgeType,
    val earnedDate: Long,
    val title: String,
    val description: String
)
