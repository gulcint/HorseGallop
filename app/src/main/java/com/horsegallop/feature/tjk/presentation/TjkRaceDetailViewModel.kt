package com.horsegallop.feature.tjk.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.tjk.model.TjkRaceCard
import com.horsegallop.domain.tjk.usecase.GetTjkRaceCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TjkRaceDetailUiState(
    val isLoading: Boolean = true,
    val raceCard: TjkRaceCard? = null,
    val selectedRaceIndex: Int = 0,
    val error: String? = null
)

@HiltViewModel
class TjkRaceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRaceCardUseCase: GetTjkRaceCardUseCase
) : ViewModel() {

    private val hippodromeCode: String = checkNotNull(savedStateHandle["hippodromeCode"])
    private val raceIndex: Int = checkNotNull(savedStateHandle.get<String>("raceIndex"))?.toIntOrNull() ?: 0

    private val _ui = MutableStateFlow(TjkRaceDetailUiState(selectedRaceIndex = raceIndex))
    val ui: StateFlow<TjkRaceDetailUiState> = _ui

    init {
        loadRaceCard()
    }

    private fun loadRaceCard() {
        _ui.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getRaceCardUseCase(date = null, hippodrome = hippodromeCode, type = "program")
                .onSuccess { card ->
                    _ui.update { it.copy(isLoading = false, raceCard = card) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
