package com.horsegallop.feature.barnmanagement.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.barnmanagement.model.StudentRosterEntry
import com.horsegallop.domain.barnmanagement.usecase.GetLessonRosterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LessonRosterUiState(
    val loading: Boolean = true,
    val roster: List<StudentRosterEntry> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class LessonRosterViewModel @Inject constructor(
    private val getLessonRosterUseCase: GetLessonRosterUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val lessonId: String = checkNotNull(savedStateHandle["lessonId"])

    private val _ui = MutableStateFlow(LessonRosterUiState())
    val ui: StateFlow<LessonRosterUiState> = _ui.asStateFlow()

    init {
        loadRoster()
    }

    fun loadRoster() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            getLessonRosterUseCase(lessonId)
                .onSuccess { roster ->
                    _ui.update { it.copy(loading = false, roster = roster) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(loading = false, error = "Katılımcılar yüklenemedi. Lütfen tekrar deneyin.") }
                }
        }
    }
}
