package com.horsegallop.feature.tbf.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.tbf.model.TbfVenue
import com.horsegallop.domain.tbf.model.TbfEventCard
import com.horsegallop.domain.tbf.model.TbfEventDay
import com.horsegallop.domain.tbf.usecase.GetTbfEventCardUseCase
import com.horsegallop.domain.tbf.usecase.GetTbfEventDayUseCase
import com.horsegallop.domain.tbf.usecase.GetTbfUpcomingEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TbfViewMode { PROGRAM, RESULTS }

data class TbfUiState(
    val isLoading: Boolean = true,
    val upcomingDays: List<TbfEventDay> = emptyList(),
    val selectedDate: String? = null,
    val venues: List<TbfVenue> = emptyList(),
    val selectedVenue: String? = null,
    val eventCard: TbfEventCard? = null,
    val isLoadingCard: Boolean = false,
    val viewMode: TbfViewMode = TbfViewMode.PROGRAM,
    val error: String? = null
)

@HiltViewModel
class TbfViewModel @Inject constructor(
    private val getEventDayUseCase: GetTbfEventDayUseCase,
    private val getEventCardUseCase: GetTbfEventCardUseCase,
    private val getUpcomingEventsUseCase: GetTbfUpcomingEventsUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow(TbfUiState())
    val ui: StateFlow<TbfUiState> = _ui

    init {
        loadUpcoming()
        loadToday("program")
    }

    private fun loadUpcoming() {
        viewModelScope.launch {
            getUpcomingEventsUseCase()
                .onSuccess { days ->
                    _ui.update { it.copy(upcomingDays = days) }
                }
                .onFailure { /* silently ignore upcoming events load failure */ }
        }
    }

    private fun loadToday(type: String) {
        _ui.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getEventDayUseCase(date = null, type = type)
                .onSuccess { eventDay ->
                    val firstVenue = eventDay.venues.firstOrNull()
                    _ui.update { state ->
                        state.copy(
                            isLoading = false,
                            venues = eventDay.venues,
                            selectedDate = eventDay.date,
                            selectedVenue = firstVenue?.code
                        )
                    }
                    if (firstVenue != null) {
                        loadEventCard(null, firstVenue.code, type)
                    }
                }
                .onFailure { e ->
                    _ui.update { it.copy(isLoading = false, error = "Veriler yüklenemedi. Lütfen tekrar deneyin.") }
                }
        }
    }

    fun selectVenue(code: String) {
        _ui.update { it.copy(selectedVenue = code) }
        loadEventCard(_ui.value.selectedDate, code, _ui.value.viewMode.name.lowercase())
    }

    fun switchMode(mode: TbfViewMode) {
        _ui.update { it.copy(viewMode = mode, eventCard = null) }
        val venue = _ui.value.selectedVenue ?: return
        loadEventCard(_ui.value.selectedDate, venue, mode.name.lowercase())
    }

    private fun loadEventCard(date: String?, venue: String, type: String) {
        _ui.update { it.copy(isLoadingCard = true) }
        viewModelScope.launch {
            getEventCardUseCase(date = date, venue = venue, type = type)
                .onSuccess { card ->
                    _ui.update { it.copy(isLoadingCard = false, eventCard = card) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(isLoadingCard = false, error = "Veriler yüklenemedi. Lütfen tekrar deneyin.") }
                }
        }
    }
}
