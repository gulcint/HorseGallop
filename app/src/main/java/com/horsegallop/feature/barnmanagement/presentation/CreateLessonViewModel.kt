package com.horsegallop.feature.barnmanagement.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.barnmanagement.usecase.CreateLessonUseCase
import com.horsegallop.domain.barnmanagement.repository.CreateLessonRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateLessonUiState(
    val isSubmitting: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateLessonViewModel @Inject constructor(
    private val createLessonUseCase: CreateLessonUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val barnId: String = checkNotNull(savedStateHandle["barnId"])

    private val _ui = MutableStateFlow(CreateLessonUiState())
    val ui: StateFlow<CreateLessonUiState> = _ui.asStateFlow()

    fun createLesson(
        title: String,
        instructorName: String,
        startTimeMs: Long,
        durationMin: Int,
        level: String,
        price: Double,
        spotsTotal: Int
    ) {
        viewModelScope.launch {
            _ui.update { it.copy(isSubmitting = true, error = null) }
            createLessonUseCase(
                CreateLessonRequest(
                    barnId = barnId,
                    title = title,
                    instructorName = instructorName,
                    startTimeMs = startTimeMs,
                    durationMin = durationMin,
                    level = level,
                    price = price,
                    spotsTotal = spotsTotal
                )
            )
                .onSuccess {
                    _ui.update { it.copy(isSubmitting = false, success = true) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(isSubmitting = false, error = e.message) }
                }
        }
    }

    fun clearError() {
        _ui.update { it.copy(error = null) }
    }
}
