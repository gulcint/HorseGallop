package com.horsegallop.feature.schedule.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.schedule.model.Lesson
import com.horsegallop.domain.schedule.usecase.GetLessonsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val getLessonsUseCase: GetLessonsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            try {
                getLessonsUseCase().collect { lessons ->
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        lessons = lessons,
                        isEmpty = lessons.isEmpty()
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    lessons = emptyList(),
                    isEmpty = true,
                    error = e.localizedMessage ?: "Failed to load lessons"
                )
            }
        }
    }
}

data class ScheduleUiState(
    val loading: Boolean = true,
    val lessons: List<Lesson> = emptyList(),
    val isEmpty: Boolean = false,
    val error: String? = null
)
