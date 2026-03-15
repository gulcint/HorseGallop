package com.horsegallop.feature.barnmanagement.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.barnmanagement.model.BarnStats
import com.horsegallop.domain.barnmanagement.model.ManagedLesson
import com.horsegallop.domain.barnmanagement.usecase.CancelLessonUseCase
import com.horsegallop.domain.barnmanagement.usecase.GetBarnStatsUseCase
import com.horsegallop.domain.barnmanagement.usecase.GetManagedLessonsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BarnDashboardUiState(
    val loading: Boolean = true,
    val barnId: String = "",
    val stats: BarnStats? = null,
    val lessons: List<ManagedLesson> = emptyList(),
    val error: String? = null,
    val cancellingLessonId: String? = null
)

@HiltViewModel
class BarnDashboardViewModel @Inject constructor(
    private val getBarnStatsUseCase: GetBarnStatsUseCase,
    private val getManagedLessonsUseCase: GetManagedLessonsUseCase,
    private val cancelLessonUseCase: CancelLessonUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val barnId: String = checkNotNull(savedStateHandle["barnId"])

    private val _ui = MutableStateFlow(BarnDashboardUiState(barnId = barnId))
    val ui: StateFlow<BarnDashboardUiState> = _ui.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            val statsResult = getBarnStatsUseCase(barnId)
            val lessonsResult = getManagedLessonsUseCase(barnId)

            _ui.update { state ->
                state.copy(
                    loading = false,
                    stats = statsResult.getOrNull(),
                    lessons = lessonsResult.getOrElse { emptyList() },
                    error = statsResult.exceptionOrNull()?.message
                        ?: lessonsResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun cancelLesson(lessonId: String) {
        viewModelScope.launch {
            _ui.update { it.copy(cancellingLessonId = lessonId) }
            cancelLessonUseCase(lessonId)
                .onSuccess {
                    _ui.update { state ->
                        state.copy(
                            cancellingLessonId = null,
                            lessons = state.lessons.map { lesson ->
                                if (lesson.id == lessonId) lesson.copy(isCancelled = true) else lesson
                            }
                        )
                    }
                }
                .onFailure { e ->
                    _ui.update { it.copy(cancellingLessonId = null, error = e.message) }
                }
        }
    }

    fun clearError() {
        _ui.update { it.copy(error = null) }
    }
}
