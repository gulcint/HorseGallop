package com.horsegallop.feature.tjk.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.tjk.model.TjkCity
import com.horsegallop.domain.tjk.model.TjkRace
import com.horsegallop.domain.tjk.model.TjkRaceDay
import com.horsegallop.domain.tjk.usecase.GetTjkCitiesUseCase
import com.horsegallop.domain.tjk.usecase.GetTjkRaceDayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class TjkUiState(
    val isLoadingCities: Boolean = true,
    val isLoadingRaces: Boolean = false,
    val cities: List<TjkCity> = emptyList(),
    val selectedCity: TjkCity? = null,
    val selectedDate: String = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date()),
    val raceDay: TjkRaceDay? = null,
    val expandedRaceNo: Int? = null,
    val error: String? = null
)

@HiltViewModel
class TjkRacesViewModel @Inject constructor(
    private val getTjkRaceDayUseCase: GetTjkRaceDayUseCase,
    private val getTjkCitiesUseCase: GetTjkCitiesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TjkUiState())
    val uiState: StateFlow<TjkUiState> = _uiState.asStateFlow()

    init {
        loadCities()
    }

    private fun loadCities() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCities = true, error = null) }
            getTjkCitiesUseCase()
                .onSuccess { cities ->
                    val istanbul = cities.firstOrNull { it.id == 3 } ?: cities.firstOrNull()
                    _uiState.update {
                        it.copy(
                            isLoadingCities = false,
                            cities = cities,
                            selectedCity = istanbul
                        )
                    }
                    istanbul?.let { loadRaces() }
                }
                .onFailure { e ->
                    // Fallback: use hardcoded cities if cloud function unreachable
                    val fallback = listOf(
                        TjkCity(3, "İstanbul"),
                        TjkCity(2, "İzmir"),
                        TjkCity(5, "Ankara"),
                        TjkCity(4, "Bursa"),
                        TjkCity(1, "Adana"),
                        TjkCity(9, "Kocaeli"),
                        TjkCity(6, "Urfa"),
                        TjkCity(7, "Elazığ"),
                        TjkCity(8, "Diyarbakır")
                    )
                    val istanbul = fallback.first()
                    _uiState.update {
                        it.copy(
                            isLoadingCities = false,
                            cities = fallback,
                            selectedCity = istanbul
                        )
                    }
                    loadRaces()
                }
        }
    }

    fun onCitySelected(city: TjkCity) {
        _uiState.update { it.copy(selectedCity = city, raceDay = null, expandedRaceNo = null) }
        loadRaces()
    }

    fun onDateSelected(date: String) {
        _uiState.update { it.copy(selectedDate = date, raceDay = null, expandedRaceNo = null) }
        loadRaces()
    }

    fun onRaceExpanded(raceNo: Int) {
        _uiState.update { state ->
            state.copy(expandedRaceNo = if (state.expandedRaceNo == raceNo) null else raceNo)
        }
    }

    fun refresh() {
        loadRaces()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun loadRaces() {
        val city = _uiState.value.selectedCity ?: return
        val date = _uiState.value.selectedDate
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRaces = true, error = null) }
            getTjkRaceDayUseCase(date, city.id)
                .onSuccess { raceDay ->
                    _uiState.update {
                        it.copy(isLoadingRaces = false, raceDay = raceDay, expandedRaceNo = null)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoadingRaces = false, error = e.message)
                    }
                }
        }
    }
}
