package com.horsegallop.feature.ride.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.ride.model.RideSession
import com.horsegallop.domain.ride.repository.RideHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RideDetailUiState(
    val ride: RideSession? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class RideDetailViewModel @Inject constructor(
    private val repository: RideHistoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(RideDetailUiState())
    val uiState: StateFlow<RideDetailUiState> = _uiState.asStateFlow()

    init {
        val rideId = savedStateHandle.get<String>("id")
        if (rideId != null) {
            loadRide(rideId)
        }
    }

    private fun loadRide(id: String) {
        viewModelScope.launch {
            repository.getRide(id).collect { ride ->
                _uiState.value = RideDetailUiState(
                    ride = ride,
                    isLoading = false
                )
            }
        }
    }
}
