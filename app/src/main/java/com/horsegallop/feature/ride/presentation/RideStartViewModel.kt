package com.horsegallop.feature.ride.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.domain.ride.model.GeoPoint
import com.horsegallop.domain.ride.model.RideMetrics
import com.horsegallop.domain.ride.usecase.SetAutoDetectUseCase
import com.horsegallop.domain.ride.usecase.StartRideUseCase
import com.horsegallop.domain.ride.usecase.StopRideUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RideStartViewModel @Inject constructor(
    private val startRideUseCase: StartRideUseCase,
    private val stopRideUseCase: StopRideUseCase,
    private val setAutoDetectUseCase: SetAutoDetectUseCase
) : ViewModel() {

    // UI State
    data class RideStartUiState(
        val isRiding: Boolean = false,
        val distanceKm: Float = 0f,
        val speedKmh: Float = 0f,
        val calories: Int = 0,
        val durationSec: Int = 0,
        val pathPoints: List<GeoPoint> = emptyList(),
        val selectedBarn: BarnWithLocation? = null,
        val barns: List<BarnWithLocation> = emptyList(),
        val autoDetectEnabled: Boolean = false,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    private val _uiState = kotlinx.coroutines.flow.MutableStateFlow(
        RideStartUiState()
    )
    val uiState: StateFlow<RideStartUiState> = _uiState

    init {
        loadBarns()
    }

    private fun loadBarns() {
        // TODO: Implement barn loading from repository
        // For now, using mock data
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Mock barns
            val mockBarns = listOf(
                BarnWithLocation(
                    barn = com.horsegallop.domain.barn.model.BarnUi(
                        id = "barn_1",
                        name = "Downtown Barn",
                        description = "Modern barn in the city center",
                        location = "123 Main Street",
                        tags = listOf("Parking", "Lockers", "Showers"),
                        rating = 4.8,
                        heroImageUrl = null,
                        lat = 41.0082,
                        lng = 28.9784
                    ),
                    lat = 41.0082,
                    lng = 28.9784,
                    amenities = setOf("Parking", "Lockers")
                ),
                BarnWithLocation(
                    barn = com.horsegallop.domain.barn.model.BarnUi(
                        id = "barn_2",
                        name = "Park Barn",
                        description = "Barn near Central Park",
                        location = "Central Park West",
                        tags = listOf("Parking", "Water"),
                        rating = 4.5,
                        heroImageUrl = null,
                        lat = 40.7829,
                        lng = -73.9654
                    ),
                    lat = 40.7829,
                    lng = -73.9654,
                    amenities = setOf("Parking", "Water")
                )
            )
            
            _uiState.value = _uiState.value.copy(
                barns = mockBarns,
                isLoading = false
            )
        }
    }

    fun startRide() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                startRideUseCase(70f) // Default weight 70kg
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to start ride"
                )
            }
        }
    }

    fun stopRide() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                stopRideUseCase(_uiState.value.selectedBarn?.barn?.name)
                _uiState.value = _uiState.value.copy(
                    isRiding = false,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to stop ride"
                )
            }
        }
    }

    fun saveCurrentRide(barnName: String?) {
        // This is handled by stopRide, but can be used for manual save
        stopRide()
    }

    fun toggleAutoDetect() {
        viewModelScope.launch {
            setAutoDetectUseCase(!_uiState.value.autoDetectEnabled)
            _uiState.value = _uiState.value.copy(
                autoDetectEnabled = !_uiState.value.autoDetectEnabled
            )
        }
    }

    fun selectBarn(barn: BarnWithLocation) {
        _uiState.value = _uiState.value.copy(selectedBarn = barn)
    }

    fun onRideMetricsUpdated(metrics: RideMetrics) {
        _uiState.value = _uiState.value.copy(
            distanceKm = metrics.distanceKm,
            speedKmh = metrics.speedKmh,
            calories = metrics.calories,
            durationSec = metrics.durationSec,
            pathPoints = metrics.pathPoints
        )
    }

    fun onRideStatusChanged(isRiding: Boolean) {
        _uiState.value = _uiState.value.copy(isRiding = isRiding)
    }

    companion object {
        val DEFAULT_WEIGHT_KG = 70f
    }
}
