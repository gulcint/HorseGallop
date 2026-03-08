package com.horsegallop.domain.review.usecase

import com.horsegallop.domain.review.model.Review
import com.horsegallop.domain.review.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMyReviewsUseCase @Inject constructor(private val repository: ReviewRepository) {
    operator fun invoke(): Flow<List<Review>> = repository.getMyReviews()
}
