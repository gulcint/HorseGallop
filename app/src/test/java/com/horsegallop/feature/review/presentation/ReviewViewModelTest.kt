package com.horsegallop.feature.review.presentation

import com.horsegallop.domain.review.model.Review
import com.horsegallop.domain.review.model.ReviewTargetType
import com.horsegallop.domain.review.usecase.GetMyReviewsUseCase
import com.horsegallop.domain.review.usecase.SubmitReviewUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val submitReviewUseCase: SubmitReviewUseCase = mock()
    private val getMyReviewsUseCase: GetMyReviewsUseCase = mock()

    private lateinit var viewModel: ReviewViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(getMyReviewsUseCase()).thenReturn(flowOf(emptyList()))
        viewModel = ReviewViewModel(submitReviewUseCase, getMyReviewsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── init / loadMyReviews ────────────────────────────────────────────────

    @Test
    fun `init loads reviews from getMyReviewsUseCase`() = runTest {
        val reviews = listOf(review("r1"))
        whenever(getMyReviewsUseCase()).thenReturn(flowOf(reviews))
        viewModel = ReviewViewModel(submitReviewUseCase, getMyReviewsUseCase)

        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.myReviews.size)
    }

    @Test
    fun `init with empty reviews sets myReviews to empty`() = runTest {
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.myReviews.isEmpty())
    }

    // ─── submitReview ────────────────────────────────────────────────────────

    @Test
    fun `submitReview on success sets submitSuccess true`() = runTest {
        whenever(
            submitReviewUseCase("target1", ReviewTargetType.LESSON, "Sabah Dersi", 5, "Harika")
        ).thenReturn(Result.success(review("r1")))

        advanceUntilIdle()

        viewModel.submitReview("target1", ReviewTargetType.LESSON, "Sabah Dersi", 5, "Harika")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.submitSuccess)
        assertFalse(viewModel.uiState.value.submitting)
        assertNull(viewModel.uiState.value.submitError)
    }

    @Test
    fun `submitReview on failure sets submitError`() = runTest {
        whenever(submitReviewUseCase(any(), any(), any(), any(), any())).thenReturn(
            Result.failure(RuntimeException("Gönderme hatası"))
        )

        advanceUntilIdle()

        viewModel.submitReview("target1", ReviewTargetType.INSTRUCTOR, "Ali Hoca", 4, "")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.submitting)
        assertFalse(viewModel.uiState.value.submitSuccess)
        assertNotNull(viewModel.uiState.value.submitError)
    }

    @Test
    fun `submitReview resets submitting to false after completion`() = runTest {
        whenever(submitReviewUseCase(any(), any(), any(), any(), any())).thenReturn(
            Result.success(review("r1"))
        )

        advanceUntilIdle()

        viewModel.submitReview("t1", ReviewTargetType.LESSON, "Ders", 3, "Orta")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.submitting)
    }

    // ─── clearSubmitState ─────────────────────────────────────────────────────

    @Test
    fun `clearSubmitState resets submitSuccess and submitError`() = runTest {
        whenever(submitReviewUseCase(any(), any(), any(), any(), any())).thenReturn(
            Result.success(review("r1"))
        )

        advanceUntilIdle()

        viewModel.submitReview("t1", ReviewTargetType.LESSON, "Ders", 5, "İyi")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.submitSuccess)

        viewModel.clearSubmitState()

        assertFalse(viewModel.uiState.value.submitSuccess)
        assertNull(viewModel.uiState.value.submitError)
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private fun review(id: String) = Review(
        id = id, targetId = "target1", targetType = ReviewTargetType.LESSON,
        targetName = "Test Dersi", rating = 5, comment = "Güzel"
    )
}
