package com.horsegallop.feature.tbf.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.tbf.model.TbfEventCard
import com.horsegallop.domain.tbf.usecase.GetTbfEventCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TbfEventDetailUiState(
    val isLoading: Boolean = true,
    val eventCard: TbfEventCard? = null,
    val selectedEventIndex: Int = 0,
    val error: String? = null
)

@HiltViewModel
class TbfEventDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getEventCardUseCase: GetTbfEventCardUseCase
) : ViewModel() {

    private val venueCode: String = checkNotNull(savedStateHandle["venueCode"])
    private val eventIndex: Int = savedStateHandle.get<String>("eventIndex")?.toIntOrNull() ?: 0

    private val _ui = MutableStateFlow(TbfEventDetailUiState(selectedEventIndex = eventIndex))
    val ui: StateFlow<TbfEventDetailUiState> = _ui

    init {
        loadEventCard()
    }

    private fun loadEventCard() {
        _ui.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getEventCardUseCase(date = null, venue = venueCode, type = "program")
                .onSuccess { card ->
                    _ui.update { it.copy(isLoading = false, eventCard = card) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
