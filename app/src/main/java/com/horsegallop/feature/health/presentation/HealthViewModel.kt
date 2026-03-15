package com.horsegallop.feature.health.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.health.model.HealthEvent
import com.horsegallop.domain.health.usecase.DeleteHealthEventUseCase
import com.horsegallop.domain.health.usecase.GetHealthEventsUseCase
import com.horsegallop.domain.health.usecase.SaveHealthEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HealthUiState(
    val loading: Boolean = true,
    val events: List<HealthEvent> = emptyList(),
    val error: String? = null,
    val selectedHorseId: String? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val getHealthEventsUseCase: GetHealthEventsUseCase,
    private val saveHealthEventUseCase: SaveHealthEventUseCase,
    private val deleteHealthEventUseCase: DeleteHealthEventUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow(HealthUiState())
    val ui: StateFlow<HealthUiState> = _ui.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() {
        _ui.update { it.copy(loading = true, error = null) }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getHealthEventsUseCase(_ui.value.selectedHorseId)
                .collect { events ->
                    _ui.update { it.copy(loading = false, events = events) }
                }
        }
    }

    fun filterByHorse(horseId: String?) {
        _ui.update { it.copy(selectedHorseId = horseId) }
        load()
    }

    fun saveEvent(event: HealthEvent) {
        _ui.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            saveHealthEventUseCase(event)
                .onSuccess { _ui.update { it.copy(isSaving = false) } }
                .onFailure { e -> _ui.update { it.copy(isSaving = false, error = e.localizedMessage) } }
        }
    }

    fun markCompleted(event: HealthEvent) {
        viewModelScope.launch {
            val updated = event.copy(
                isCompleted = true,
                completedDate = System.currentTimeMillis()
            )
            saveHealthEventUseCase(updated)
                .onFailure { e -> _ui.update { it.copy(error = e.localizedMessage) } }
        }
    }

    fun delete(eventId: String) {
        viewModelScope.launch {
            deleteHealthEventUseCase(eventId)
                .onFailure { e -> _ui.update { it.copy(error = e.localizedMessage) } }
        }
    }

    fun clearError() = _ui.update { it.copy(error = null) }
}
