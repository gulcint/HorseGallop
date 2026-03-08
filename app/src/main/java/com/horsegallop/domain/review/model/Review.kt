package com.horsegallop.domain.review.model

data class Review(
    val id: String,
    val targetId: String,          // lessonId veya instructorId
    val targetType: ReviewTargetType,
    val targetName: String,
    val rating: Int,               // 1-5
    val comment: String,
    val createdAt: String = "",
    val authorName: String = ""
)

enum class ReviewTargetType { LESSON, INSTRUCTOR }
