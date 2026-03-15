package com.horsegallop.feature.ride.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.R
import com.horsegallop.domain.auth.usecase.GetCurrentUserIdUseCase
import com.horsegallop.domain.auth.usecase.GetUserProfileUseCase
import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.domain.barn.repository.BarnRepository
import com.horsegallop.domain.content.usecase.GetAppContentUseCase
import com.horsegallop.domain.ride.model.RideSyncStatus
import com.horsegallop.domain.safety.usecase.GetSafetySettingsUseCase
import com.horsegallop.domain.safety.usecase.TriggerSafetyAlarmUseCase
import com.horsegallop.domain.ride.usecase.ObserveAutoStopSignalUseCase
import com.horsegallop.domain.ride.usecase.ObserveIsRidingUseCase
import com.horsegallop.domain.ride.usecase.ObservePendingRideSyncCountUseCase
import com.horsegallop.domain.ride.usecase.ObserveRideMetricsUseCase
import com.horsegallop.domain.ride.usecase.RetryPendingRideSyncUseCase
import com.horsegallop.domain.ride.usecase.SetAutoDetectUseCase
import com.horsegallop.domain.ride.usecase.StartRideUseCase
import com.horsegallop.domain.ride.usecase.StopRideUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

@HiltViewModel
class RideTrackingViewModel @Inject constructor(
    private val startRideUseCase: StartRideUseCase,
    private val stopRideUseCase: StopRideUseCase,
    private val observeRideMetricsUseCase: ObserveRideMetricsUseCase,
    private val observeIsRidingUseCase: ObserveIsRidingUseCase,
    private val observePendingRideSyncCountUseCase: ObservePendingRideSyncCountUseCase,
    private val retryPendingRideSyncUseCase: RetryPendingRideSyncUseCase,
    private val setAutoDetectUseCase: SetAutoDetectUseCase,
    private val observeAutoStopSignalUseCase: ObserveAutoStopSignalUseCase,
    private val barnRepository: BarnRepository,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getAppContentUseCase: GetAppContentUseCase,
    private val getSafetySettingsUseCase: GetSafetySettingsUseCase,
    private val triggerSafetyAlarmUseCase: TriggerSafetyAlarmUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RideUiState())
    val uiState: StateFlow<RideUiState> = _uiState.asStateFlow()

    private var userWeightKg: Float = 70f

    init {
        combine(
            observeIsRidingUseCase(),
            observeRideMetricsUseCase()
        ) { isRiding, metrics ->
            isRiding to metrics
        }.onEach { (isRiding, metrics) ->
            _uiState.update { previous ->
                val averageSpeed = calculateAverageSpeed(metrics.distanceKm, metrics.durationSec)
                val nextMaxSpeed = if (isRiding) {
                    maxOf(previous.maxSpeedKmh, metrics.speedKmh)
                } else {
                    previous.maxSpeedKmh
                }
                previous.copy(
                    isRiding = isRiding,
                    speedKmh = metrics.speedKmh,
                    avgSpeedKmh = averageSpeed,
                    maxSpeedKmh = nextMaxSpeed,
                    distanceKm = metrics.distanceKm,
                    durationSec = metrics.durationSec,
                    calories = metrics.calories,
                    horseCalories = metrics.horseCalories,
                    altitudeM = metrics.altitudeM,
                    pathPoints = metrics.pathPoints
                )
            }
        }.launchIn(viewModelScope)

        barnRepository.getBarns().onEach { barns ->
            _uiState.update { state ->
                val selectedBarnId = state.selectedBarn?.barn?.id
                val resolvedSelection = selectedBarnId?.let { currentId ->
                    barns.firstOrNull { it.barn.id == currentId }
                } ?: barns.firstOrNull()
                state.copy(
                    barns = barns,
                    selectedBarn = resolvedSelection
                )
            }
        }.launchIn(viewModelScope)

        observePendingRideSyncCountUseCase().onEach { pendingCount ->
            _uiState.update { state ->
                val resolvedStatus = when {
                    pendingCount == 0 && state.lastStopSyncStatus == RideSyncStatus.Pending -> RideSyncStatus.Synced
                    else -> state.lastStopSyncStatus
                }
                state.copy(
                    pendingSyncCount = pendingCount,
                    lastStopSyncStatus = resolvedStatus
                )
            }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            runCatching { retryPendingRideSyncUseCase() }
        }

        loadUserProfileWeight()
        loadDynamicContent()
        loadSafetySettings()

        // Auto-stop + safety alarm: show dialogs when stillness signal fires
        observeAutoStopSignalUseCase()
            .onEach {
                _uiState.update { s ->
                    s.copy(
                        showAutoStopDialog = true,
                        showSafetyAlarmDialog = s.safetyEnabled
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun dismissAutoStopDialog() {
        _uiState.update { it.copy(showAutoStopDialog = false) }
    }

    fun confirmAutoStop() {
        _uiState.update { it.copy(showAutoStopDialog = false) }
        finishRide()
    }

    fun dismissSafetyAlarmDialog() {
        _uiState.update { it.copy(showSafetyAlarmDialog = false) }
    }

    fun confirmSafetyAlarm() {
        _uiState.update { it.copy(showSafetyAlarmDialog = false) }
        val lastPoint = _uiState.value.pathPoints.lastOrNull()
        if (lastPoint != null) {
            viewModelScope.launch {
                runCatching { triggerSafetyAlarmUseCase(lastPoint.latitude, lastPoint.longitude) }
            }
        }
    }

    fun onRideTypeSelected(rideType: RideType) {
        _uiState.update { it.copy(selectedRideType = rideType) }
    }

    fun onBarnSelected(barn: BarnWithLocation) {
        _uiState.update { it.copy(selectedBarn = barn) }
    }

    fun onToggleRide(hasLocationPermission: Boolean) {
        if (_uiState.value.isRiding) {
            finishRide()
        } else {
            startRide(hasLocationPermission)
        }
    }

    fun onSetAutoDetect(enabled: Boolean) {
        viewModelScope.launch {
            setAutoDetectUseCase(enabled)
            _uiState.update { it.copy(autoDetect = enabled) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessageResId = null) }
    }

    fun dismissSavedSummary() {
        _uiState.update { it.copy(savedRideSummary = null) }
    }

    fun onRetryPendingSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRetryingSync = true) }
            try {
                retryPendingRideSyncUseCase()
            } catch (_: Exception) {
                _uiState.update { it.copy(errorMessageResId = R.string.error_network) }
            } finally {
                _uiState.update { it.copy(isRetryingSync = false) }
            }
        }
    }

    private fun startRide(hasLocationPermission: Boolean) {
        if (!hasLocationPermission) {
            _uiState.update { it.copy(errorMessageResId = R.string.ride_permission_required) }
            return
        }

        viewModelScope.launch {
            try {
                val rideType = _uiState.value.selectedRideType.backendValue
                _uiState.update {
                    it.copy(
                        errorMessageResId = null,
                        savedRideSummary = null,
                        maxSpeedKmh = 0f,
                        avgSpeedKmh = 0f,
                        lastStopSyncStatus = null
                    )
                }
                startRideUseCase(userWeightKg, rideType)
            } catch (_: Exception) {
                _uiState.update { it.copy(errorMessageResId = R.string.error_unknown) }
            }
        }
    }

    private fun finishRide() {
        val snapshot = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val stopResult = stopRideUseCase(snapshot.selectedBarn?.barn?.name)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessageResId = null,
                        lastStopSyncStatus = when {
                            stopResult.remoteSynced -> RideSyncStatus.Synced
                            stopResult.pendingSyncId != null -> RideSyncStatus.Pending
                            else -> RideSyncStatus.Failed
                        },
                        savedRideSummary = SavedRideSummary(
                            durationSec = snapshot.durationSec,
                            distanceKm = snapshot.distanceKm,
                            calories = snapshot.calories,
                            avgSpeedKmh = snapshot.avgSpeedKmh,
                            maxSpeedKmh = snapshot.maxSpeedKmh,
                            rideType = snapshot.selectedRideType,
                            barnName = snapshot.selectedBarn?.barn?.name,
                            savedAtMillis = System.currentTimeMillis()
                        )
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessageResId = R.string.error_unknown
                    )
                }
            }
        }
    }

    private fun calculateAverageSpeed(distanceKm: Float, durationSec: Int): Float {
        if (durationSec <= 0) return 0f
        val hours = durationSec / 3600f
        if (hours <= 0f) return 0f
        return distanceKm / hours
    }

    private fun loadUserProfileWeight() {
        val uid = getCurrentUserIdUseCase() ?: return
        viewModelScope.launch {
            getUserProfileUseCase(uid).collect { result ->
                result.onSuccess { profile ->
                    userWeightKg = profile.weight ?: 70f
                }
            }
        }
    }

    private fun loadDynamicContent() {
        val locale = Locale.getDefault().language
        viewModelScope.launch {
            getAppContentUseCase(locale).collect { result ->
                result.onSuccess { content ->
                    _uiState.update {
                        it.copy(
                            liveTitle = content.rideLiveTitle,
                            liveSubtitleIdle = content.rideLiveSubtitleIdle,
                            liveSubtitleActive = content.rideLiveSubtitleActive,
                            permissionTitle = content.ridePermissionTitle,
                            permissionHint = content.ridePermissionHint,
                            grantLocationCta = content.rideGrantLocationCta
                        )
                    }
                }
            }
        }
    }

    private fun loadSafetySettings() {
        viewModelScope.launch {
            runCatching { getSafetySettingsUseCase() }
                .getOrNull()
                ?.onSuccess { settings ->
                    _uiState.update { it.copy(safetyEnabled = settings.isEnabled) }
                }
        }
    }
}
