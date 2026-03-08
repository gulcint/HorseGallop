package com.horsegallop.domain.review.repository

import com.horsegallop.domain.review.model.Review
import com.horsegallop.domain.review.model.ReviewTargetType
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun getMyReviews(): Flow<List<Review>>
    suspend fun submitReview(
        targetId: String,
        targetType: ReviewTargetType,
        targetName: String,
        rating: Int,
        comment: String
    ): Result<Review>
}
