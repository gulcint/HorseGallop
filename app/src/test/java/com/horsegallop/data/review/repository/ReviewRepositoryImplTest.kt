package com.horsegallop.data.review.repository

import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.data.remote.supabase.SupabaseReviewDto
import com.horsegallop.domain.review.model.ReviewTargetType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ReviewRepositoryImplTest {

    private val dataSource: SupabaseDataSource = mock()
    private lateinit var repository: ReviewRepositoryImpl

    @Before
    fun setUp() {
        repository = ReviewRepositoryImpl(dataSource)
    }

    // ─── getMyReviews ─────────────────────────────────────────────────────────

    @Test
    fun `getMyReviews emits mapped reviews`() = runTest {
        whenever(dataSource.getMyReviews()).thenReturn(
            listOf(reviewDto("r1", "instructor"))
        )

        val result = repository.getMyReviews().first()

        assertEquals(1, result.size)
        assertEquals("r1", result[0].id)
        assertEquals(ReviewTargetType.INSTRUCTOR, result[0].targetType)
    }

    @Test
    fun `getMyReviews maps instructor targetType correctly`() = runTest {
        whenever(dataSource.getMyReviews()).thenReturn(
            listOf(reviewDto("r1", "instructor"))
        )

        val review = repository.getMyReviews().first().first()

        assertEquals(ReviewTargetType.INSTRUCTOR, review.targetType)
    }

    @Test
    fun `getMyReviews maps non-instructor targetType to LESSON`() = runTest {
        whenever(dataSource.getMyReviews()).thenReturn(
            listOf(reviewDto("r1", "lesson"))
        )

        val review = repository.getMyReviews().first().first()

        assertEquals(ReviewTargetType.LESSON, review.targetType)
    }

    @Test
    fun `getMyReviews emits empty list when dataSource throws`() = runTest {
        whenever(dataSource.getMyReviews()).thenThrow(RuntimeException("network error"))

        val result = repository.getMyReviews().first()

        assertTrue(result.isEmpty())
    }

    // ─── submitReview ─────────────────────────────────────────────────────────

    @Test
    fun `submitReview with INSTRUCTOR type returns success`() = runTest {
        whenever(dataSource.submitReview(any())).thenReturn(reviewDto("r1", "instructor"))

        val result = repository.submitReview(
            targetId = "inst1",
            targetType = ReviewTargetType.INSTRUCTOR,
            targetName = "Ali Hoca",
            rating = 5,
            comment = "Harika"
        )

        assertTrue(result.isSuccess)
        assertEquals("r1", result.getOrNull()?.id)
        assertEquals(ReviewTargetType.INSTRUCTOR, result.getOrNull()?.targetType)
    }

    @Test
    fun `submitReview with LESSON type returns success`() = runTest {
        whenever(dataSource.submitReview(any())).thenReturn(reviewDto("r2", "lesson"))

        val result = repository.submitReview(
            targetId = "les1",
            targetType = ReviewTargetType.LESSON,
            targetName = "Sabah Dersi",
            rating = 4,
            comment = "İyi"
        )

        assertTrue(result.isSuccess)
        assertEquals(ReviewTargetType.LESSON, result.getOrNull()?.targetType)
    }

    @Test
    fun `submitReview returns failure when dataSource throws`() = runTest {
        whenever(dataSource.submitReview(any())).thenThrow(RuntimeException("Submit failed"))

        val result = repository.submitReview(
            targetId = "t1", targetType = ReviewTargetType.LESSON,
            targetName = "Test", rating = 3, comment = ""
        )

        assertTrue(result.isFailure)
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private fun reviewDto(id: String, targetType: String) = SupabaseReviewDto(
        id = id, targetId = "target1", targetType = targetType,
        targetName = "Test Target", rating = 5, comment = "Güzel",
        createdAt = "2026-04-01", authorName = "Test User"
    )
}
