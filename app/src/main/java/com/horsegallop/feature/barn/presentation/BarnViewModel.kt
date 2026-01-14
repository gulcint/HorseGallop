package com.horsegallop.feature.barn.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.domain.barn.usecase.GetBarnsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BarnUiState(
    val query: String = "",
    val selectedFilters: Set<String> = emptySet(),
    val filteredBarns: List<BarnWithLocation> = emptyList(),
    val allBarns: List<BarnWithLocation> = emptyList(),
    val availableFilters: List<String> = listOf(
        "cafe", "indoor_arena", "outdoor_arena", "parking", "lessons",
        "boarding", "vet", "farrier", "lighting", "trail", "open_now"
    )
)

@HiltViewModel
class BarnViewModel @Inject constructor(
    private val getBarnsUseCase: GetBarnsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BarnUiState())
    val uiState: StateFlow<BarnUiState> = _uiState.asStateFlow()

    init {
        loadBarns()
    }

    private fun loadBarns() {
        viewModelScope.launch {
            getBarnsUseCase().collect { barns ->
                _uiState.update { it.copy(allBarns = barns, filteredBarns = barns) }
            }
        }
    }

    fun updateQuery(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        applyFilters()
    }

    fun toggleFilter(filter: String) {
        _uiState.update { currentState ->
            val newFilters = if (currentState.selectedFilters.contains(filter)) {
                currentState.selectedFilters - filter
            } else {
                currentState.selectedFilters + filter
            }
            currentState.copy(selectedFilters = newFilters)
        }
        applyFilters()
    }

    fun clearFilters() {
        _uiState.update { it.copy(selectedFilters = emptySet()) }
        applyFilters()
    }

    fun clearQuery() {
        updateQuery("")
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        val query = currentState.query
        val filters = currentState.selectedFilters
        val all = currentState.allBarns

        val filtered = all.filter { item ->
            // Filter by query
            val matchesQuery = if (query.isBlank()) true else {
                item.barn.name.contains(query, ignoreCase = true) ||
                item.barn.description.contains(query, ignoreCase = true)
            }
            // Filter by amenities
            val matchesFilters = if (filters.isEmpty()) true else {
                filters.all { it in item.amenities }
            }
            matchesQuery && matchesFilters
        }

        _uiState.update { it.copy(filteredBarns = filtered) }
    }
}
