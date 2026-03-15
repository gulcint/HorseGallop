package com.horsegallop.feature.tjk.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.tjk.model.TjkHippodrome
import com.horsegallop.domain.tjk.model.TjkRaceCard
import com.horsegallop.domain.tjk.model.TjkRaceDay
import com.horsegallop.domain.tjk.usecase.GetTjkRaceCardUseCase
import com.horsegallop.domain.tjk.usecase.GetTjkRaceDayUseCase
import com.horsegallop.domain.tjk.usecase.GetTjkUpcomingRacesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TjkViewMode { PROGRAM, RESULTS }

data class TjkUiState(
    val isLoading: Boolean = true,
    val upcomingDays: List<TjkRaceDay> = emptyList(),
    val selectedDate: String? = null,
    val hippodromes: List<TjkHippodrome> = emptyList(),
    val selectedHippodrome: String? = null,
    val raceCard: TjkRaceCard? = null,
    val isLoadingCard: Boolean = false,
    val viewMode: TjkViewMode = TjkViewMode.PROGRAM,
    val error: String? = null
)

@HiltViewModel
class TjkViewModel @Inject constructor(
    private val getRaceDayUseCase: GetTjkRaceDayUseCase,
    private val getRaceCardUseCase: GetTjkRaceCardUseCase,
    private val getUpcomingRacesUseCase: GetTjkUpcomingRacesUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow(TjkUiState())
    val ui: StateFlow<TjkUiState> = _ui

    init {
        loadUpcoming()
        loadToday("program")
    }

    private fun loadUpcoming() {
        viewModelScope.launch {
            getUpcomingRacesUseCase()
                .onSuccess { days ->
                    _ui.update { it.copy(upcomingDays = days) }
                }
                .onFailure { /* silently ignore upcoming races load failure */ }
        }
    }

    private fun loadToday(type: String) {
        _ui.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getRaceDayUseCase(date = null, type = type)
                .onSuccess { raceDay ->
                    val firstHippodrome = raceDay.hippodromes.firstOrNull()
                    _ui.update { state ->
                        state.copy(
                            isLoading = false,
                            hippodromes = raceDay.hippodromes,
                            selectedDate = raceDay.date,
                            selectedHippodrome = firstHippodrome?.code
                        )
                    }
                    if (firstHippodrome != null) {
                        loadRaceCard(null, firstHippodrome.code, type)
                    }
                }
                .onFailure { e ->
                    _ui.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun selectHippodrome(code: String) {
        _ui.update { it.copy(selectedHippodrome = code) }
        loadRaceCard(_ui.value.selectedDate, code, _ui.value.viewMode.name.lowercase())
    }

    fun switchMode(mode: TjkViewMode) {
        _ui.update { it.copy(viewMode = mode, raceCard = null) }
        val hippodrome = _ui.value.selectedHippodrome ?: return
        loadRaceCard(_ui.value.selectedDate, hippodrome, mode.name.lowercase())
    }

    private fun loadRaceCard(date: String?, hippodrome: String, type: String) {
        _ui.update { it.copy(isLoadingCard = true) }
        viewModelScope.launch {
            getRaceCardUseCase(date = date, hippodrome = hippodrome, type = type)
                .onSuccess { card ->
                    _ui.update { it.copy(isLoadingCard = false, raceCard = card) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(isLoadingCard = false, error = e.message) }
                }
        }
    }
}
