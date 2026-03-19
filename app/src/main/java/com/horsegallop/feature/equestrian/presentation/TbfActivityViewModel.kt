package com.horsegallop.feature.equestrian.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.equestrian.model.TbfActivity
import com.horsegallop.domain.equestrian.model.TbfDiscipline
import com.horsegallop.domain.equestrian.usecase.GetTbfActivitiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@Immutable
data class TbfActivityUiState(
    val isLoading: Boolean = true,
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDay: LocalDate? = null,
    val activitiesForMonth: List<TbfActivity> = emptyList(),
    val activitiesForSelectedDay: List<TbfActivity> = emptyList(),
    val disciplineFilters: Set<TbfDiscipline> = emptySet(),
    val error: String? = null
) {
    val daysWithActivities: Set<LocalDate>
        get() = activitiesForMonth
            .filter { disciplineFilters.isEmpty() || it.discipline in disciplineFilters }
            .flatMap { activity ->
                generateSequence(activity.startDate) { d ->
                    if (d < activity.endDate) d.plusDays(1) else null
                }.toList()
            }.toSet()

    val filteredActivitiesForSelectedDay: List<TbfActivity>
        get() = if (disciplineFilters.isEmpty()) activitiesForSelectedDay
        else activitiesForSelectedDay.filter { it.discipline in disciplineFilters }
}

@HiltViewModel
class TbfActivityViewModel @Inject constructor(
    private val getActivities: GetTbfActivitiesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TbfActivityUiState())
    val uiState: StateFlow<TbfActivityUiState> = _uiState.asStateFlow()

    init {
        loadMonth(_uiState.value.currentMonth)
    }

    fun nextMonth() {
        val next = _uiState.value.currentMonth.plusMonths(1)
        _uiState.update { it.copy(currentMonth = next, selectedDay = null, activitiesForSelectedDay = emptyList()) }
        loadMonth(next)
    }

    fun previousMonth() {
        val prev = _uiState.value.currentMonth.minusMonths(1)
        _uiState.update { it.copy(currentMonth = prev, selectedDay = null, activitiesForSelectedDay = emptyList()) }
        loadMonth(prev)
    }

    fun selectDay(date: LocalDate) {
        val activities = _uiState.value.activitiesForMonth.filter { activity ->
            !date.isBefore(activity.startDate) && !date.isAfter(activity.endDate)
        }
        _uiState.update { it.copy(selectedDay = date, activitiesForSelectedDay = activities) }
    }

    fun toggleDisciplineFilter(discipline: TbfDiscipline) {
        _uiState.update { state ->
            val filters = state.disciplineFilters.toMutableSet()
            if (discipline in filters) filters.remove(discipline) else filters.add(discipline)
            state.copy(disciplineFilters = filters)
        }
    }

    fun clearAllFilters() {
        _uiState.update { it.copy(disciplineFilters = emptySet()) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun loadMonth(month: YearMonth) {
        viewModelScope.launch {
            val requestedMonth = month
            _uiState.update { it.copy(isLoading = true, error = null) }
            getActivities(month)
                .onSuccess { activities ->
                    // Guard against stale results if user navigated quickly
                    if (_uiState.value.currentMonth == requestedMonth) {
                        _uiState.update { it.copy(isLoading = false, activitiesForMonth = activities) }
                    }
                }
                .onFailure { e ->
                    if (_uiState.value.currentMonth == requestedMonth) {
                        _uiState.update { it.copy(isLoading = false, error = e.message ?: "Bilinmeyen hata") }
                    }
                }
        }
    }
}
