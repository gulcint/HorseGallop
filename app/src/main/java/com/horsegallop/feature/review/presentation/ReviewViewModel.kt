package com.horsegallop.feature.review.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.review.model.Review
import com.horsegallop.domain.review.model.ReviewTargetType
import com.horsegallop.domain.review.usecase.GetMyReviewsUseCase
import com.horsegallop.domain.review.usecase.SubmitReviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val submitReviewUseCase: SubmitReviewUseCase,
    private val getMyReviewsUseCase: GetMyReviewsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init { loadMyReviews() }

    private fun loadMyReviews() {
        viewModelScope.launch {
            try {
                getMyReviewsUseCase().collect { reviews ->
                    _uiState.value = _uiState.value.copy(myReviews = reviews)
                }
            } catch (_: Exception) { }
        }
    }

    fun submitReview(
        targetId: String,
        targetType: ReviewTargetType,
        targetName: String,
        rating: Int,
        comment: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(submitting = true, submitError = null)
            submitReviewUseCase(targetId, targetType, targetName, rating, comment)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(submitting = false, submitSuccess = true)
                    loadMyReviews()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(submitting = false, submitError = e.localizedMessage ?: "Değerlendirme gönderilemedi")
                }
        }
    }

    fun clearSubmitState() {
        _uiState.value = _uiState.value.copy(submitSuccess = false, submitError = null)
    }
}

data class ReviewUiState(
    val myReviews: List<Review> = emptyList(),
    val submitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val submitError: String? = null
)
