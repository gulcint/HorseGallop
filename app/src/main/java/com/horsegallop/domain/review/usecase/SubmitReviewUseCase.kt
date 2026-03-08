package com.horsegallop.domain.review.usecase

import com.horsegallop.domain.review.model.Review
import com.horsegallop.domain.review.model.ReviewTargetType
import com.horsegallop.domain.review.repository.ReviewRepository
import javax.inject.Inject

class SubmitReviewUseCase @Inject constructor(private val repository: ReviewRepository) {
    suspend operator fun invoke(
        targetId: String,
        targetType: ReviewTargetType,
        targetName: String,
        rating: Int,
        comment: String
    ): Result<Review> = repository.submitReview(targetId, targetType, targetName, rating, comment)
}
