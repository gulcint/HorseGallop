package com.horsegallop.data.review.repository

import com.horsegallop.data.remote.functions.AppFunctionsDataSource
import com.horsegallop.domain.review.model.Review
import com.horsegallop.domain.review.model.ReviewTargetType
import com.horsegallop.domain.review.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ReviewRepositoryImpl @Inject constructor(
    private val functionsDataSource: AppFunctionsDataSource
) : ReviewRepository {

    override fun getMyReviews(): Flow<List<Review>> = flow {
        try {
            val reviews = functionsDataSource.getMyReviews().map { dto ->
                Review(
                    id = dto.id,
                    targetId = dto.targetId,
                    targetType = if (dto.targetType == "instructor") ReviewTargetType.INSTRUCTOR else ReviewTargetType.LESSON,
                    targetName = dto.targetName,
                    rating = dto.rating,
                    comment = dto.comment,
                    createdAt = dto.createdAt,
                    authorName = dto.authorName
                )
            }
            emit(reviews)
        } catch (_: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun submitReview(
        targetId: String, targetType: ReviewTargetType, targetName: String,
        rating: Int, comment: String
    ): Result<Review> = runCatching {
        val dto = functionsDataSource.submitReview(
            targetId = targetId,
            targetType = if (targetType == ReviewTargetType.INSTRUCTOR) "instructor" else "lesson",
            targetName = targetName,
            rating = rating,
            comment = comment
        )
        Review(
            id = dto.id,
            targetId = dto.targetId,
            targetType = targetType,
            targetName = dto.targetName,
            rating = dto.rating,
            comment = dto.comment,
            createdAt = dto.createdAt,
            authorName = dto.authorName
        )
    }
}
