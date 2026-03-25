package com.horsegallop.feature.horse.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.horse.model.HorseHealthEvent
import com.horsegallop.domain.horse.model.HorseHealthEventType
import com.horsegallop.domain.horse.usecase.AddHorseHealthEventUseCase
import com.horsegallop.domain.horse.usecase.DeleteHorseHealthEventUseCase
import com.horsegallop.domain.horse.usecase.GetHorseHealthEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class HorseHealthUiState(
    val isLoading: Boolean = true,
    val events: List<HorseHealthEvent> = emptyList(),
    val upcomingEvents: List<HorseHealthEvent> = emptyList(),
    val pastEvents: List<HorseHealthEvent> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val horseName: String = ""
)

@HiltViewModel
class HorseHealthViewModel @Inject constructor(
    private val getHorseHealthEventsUseCase: GetHorseHealthEventsUseCase,
    private val addHorseHealthEventUseCase: AddHorseHealthEventUseCase,
    private val deleteHorseHealthEventUseCase: DeleteHorseHealthEventUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow(HorseHealthUiState())
    val ui: StateFlow<HorseHealthUiState> = _ui.asStateFlow()

    private var currentHorseId: String = ""

    fun load(horseId: String, horseName: String) {
        if (currentHorseId == horseId && !_ui.value.isLoading) return
        currentHorseId = horseId
        _ui.update { it.copy(isLoading = true, horseName = horseName) }
        viewModelScope.launch {
            getHorseHealthEventsUseCase(horseId)
                .onSuccess { events -> updateEvents(events) }
                .onFailure { e -> _ui.update { it.copy(isLoading = false, error = "İşlem gerçekleştirilemedi. Lütfen tekrar deneyin.") } }
        }
    }

    fun addEvent(type: HorseHealthEventType, date: String, notes: String) {
        if (currentHorseId.isBlank()) return
        _ui.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            addHorseHealthEventUseCase(currentHorseId, type, date, notes)
                .onSuccess { newEvent ->
                    val updated = (_ui.value.events + newEvent).sortedBy { it.date }
                    updateEvents(updated)
                    _ui.update { it.copy(isSaving = false) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(isSaving = false, error = "İşlem gerçekleştirilemedi. Lütfen tekrar deneyin.") }
                }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            deleteHorseHealthEventUseCase(eventId, currentHorseId)
                .onSuccess {
                    val updated = _ui.value.events.filter { it.id != eventId }
                    updateEvents(updated)
                }
                .onFailure { e ->
                    _ui.update { it.copy(error = "İşlem gerçekleştirilemedi. Lütfen tekrar deneyin.") }
                }
        }
    }

    fun clearError() = _ui.update { it.copy(error = null) }

    private fun updateEvents(events: List<HorseHealthEvent>) {
        val today = LocalDate.now()
        val thirtyDaysLater = today.plusDays(30)
        val upcoming = events.filter {
            runCatching {
                val d = LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE)
                !d.isBefore(today) && !d.isAfter(thirtyDaysLater)
            }.getOrDefault(false)
        }
        val past = events.filter {
            runCatching {
                LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE).isBefore(today)
            }.getOrDefault(false)
        }
        _ui.update { it.copy(isLoading = false, events = events, upcomingEvents = upcoming, pastEvents = past) }
    }
}

/** Kaç gün kaldığını hesaplar; geçmişse negatif, bugünse 0 */
fun HorseHealthEvent.daysUntil(): Long = runCatching {
    ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE))
}.getOrDefault(0L)
