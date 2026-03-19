package com.horsegallop.data.review.repository

import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.data.remote.supabase.SupabaseReviewDto
import com.horsegallop.domain.review.model.Review
import com.horsegallop.domain.review.model.ReviewTargetType
import com.horsegallop.domain.review.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ReviewRepositoryImpl @Inject constructor(
    private val supabaseDataSource: SupabaseDataSource
) : ReviewRepository {

    override fun getMyReviews(): Flow<List<Review>> = flow {
        emit(supabaseDataSource.getMyReviews().map { dto ->
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
        })
    }.catch { emit(emptyList()) }

    override suspend fun submitReview(
        targetId: String, targetType: ReviewTargetType, targetName: String,
        rating: Int, comment: String
    ): Result<Review> = runCatching {
        val dto = SupabaseReviewDto(
            targetId = targetId,
            targetType = if (targetType == ReviewTargetType.INSTRUCTOR) "instructor" else "lesson",
            targetName = targetName,
            rating = rating,
            comment = comment
        )
        val saved = supabaseDataSource.submitReview(dto)
        Review(
            id = saved.id,
            targetId = saved.targetId,
            targetType = targetType,
            targetName = saved.targetName,
            rating = saved.rating,
            comment = saved.comment,
            createdAt = saved.createdAt,
            authorName = saved.authorName
        )
    }
}
