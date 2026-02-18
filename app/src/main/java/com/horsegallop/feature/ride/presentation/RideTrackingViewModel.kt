package com.horsegallop.feature.ride.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.ride.model.GeoPoint
import com.horsegallop.domain.ride.model.RideSession
import com.horsegallop.domain.ride.repository.RideHistoryRepository
import com.horsegallop.domain.ride.usecase.ObserveIsRidingUseCase
import com.horsegallop.domain.ride.usecase.ObserveRideMetricsUseCase
import com.horsegallop.domain.ride.usecase.SetAutoDetectUseCase
import com.horsegallop.domain.ride.usecase.StartRideUseCase
import com.horsegallop.domain.ride.usecase.StopRideUseCase
import com.horsegallop.domain.ride.usecase.CalculateAverageCaloriesUseCase
import com.horsegallop.domain.ride.usecase.CalculateAverageSpeedUseCase
import com.horsegallop.domain.barn.repository.BarnRepository
import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.domain.auth.usecase.GetUserProfileUseCase
import com.horsegallop.domain.auth.usecase.GetCurrentUserIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class RideUiState(
  val speedKmh: Float,
  val distanceKm: Float,
  val durationSec: Int,
  val calories: Int,
  val isRiding: Boolean,
  val autoDetect: Boolean,
  val dailyTrend: List<Float>,
  val weeklyTrend: List<Float>,
  val totalCareerDistance: Float = 0f,
  val pathPoints: List<GeoPoint>,
  val barns: List<BarnWithLocation> = emptyList(),
  val selectedBarn: BarnWithLocation? = null
)

@HiltViewModel
class RideTrackingViewModel @Inject constructor(
    private val startRideUseCase: StartRideUseCase,
    private val stopRideUseCase: StopRideUseCase,
    private val observeRideMetricsUseCase: ObserveRideMetricsUseCase,
    private val observeIsRidingUseCase: ObserveIsRidingUseCase,
    private val setAutoDetectUseCase: SetAutoDetectUseCase,
    private val rideHistoryRepository: RideHistoryRepository,
    private val barnRepository: BarnRepository,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val calculateAverageCaloriesUseCase: CalculateAverageCaloriesUseCase,
    private val calculateAverageSpeedUseCase: CalculateAverageSpeedUseCase
) : ViewModel() {
  private val _uiState: MutableStateFlow<RideUiState> = MutableStateFlow(
    RideUiState(
      speedKmh = 0f,
      distanceKm = 0f,
      durationSec = 0,
      calories = 0,
      isRiding = false,
      autoDetect = false,
      dailyTrend = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
      weeklyTrend = listOf(0f, 0f, 0f, 0f),
      pathPoints = listOf(GeoPoint(41.0, 29.0)) // Default Istanbul
    )
  )
  val uiState: StateFlow<RideUiState> = _uiState
  private var userWeightKg: Float = 70f

  init {
      // Observe Metrics & Riding Status
      combine(
          observeIsRidingUseCase(),
          observeRideMetricsUseCase()
      ) { isRiding, metrics ->
          _uiState.value.copy(
              isRiding = isRiding,
              speedKmh = metrics.speedKmh,
              distanceKm = metrics.distanceKm,
              durationSec = metrics.durationSec,
              calories = metrics.calories,
              pathPoints = metrics.pathPoints
          )
      }.onEach { newState ->
          _uiState.value = newState
      }.launchIn(viewModelScope)

      // Load Barns
      barnRepository.getBarns().onEach { barns ->
          _uiState.update { it.copy(barns = barns) }
      }.launchIn(viewModelScope)

      loadUserProfile()

      // Load History (Optimized: moved calculation to IO)
      rideHistoryRepository.getRideHistory().onEach { history ->
          viewModelScope.launch(Dispatchers.Default) {
              val daily = calculateDailyTrend(history)
              val weekly = calculateWeeklyTrend(history)
              val total = history.sumOf { it.distanceKm.toDouble() }.toFloat()
              withContext(Dispatchers.Main) {
                  _uiState.update { it.copy(
                      dailyTrend = daily, 
                      weeklyTrend = weekly,
                      totalCareerDistance = total
                  ) }
              }
          }
      }.launchIn(viewModelScope)
  }

  private fun calculateDailyTrend(history: List<RideSession>): List<Float> {
      val cal = java.util.Calendar.getInstance()
      cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
      cal.set(java.util.Calendar.MINUTE, 0)
      cal.set(java.util.Calendar.SECOND, 0)
      cal.set(java.util.Calendar.MILLISECOND, 0)
      val todayMillis = cal.timeInMillis
      val oneDayMillis = 24 * 60 * 60 * 1000L
      
      val days = FloatArray(7)
      
      history.forEach { ride ->
          val rideCal = java.util.Calendar.getInstance()
          rideCal.timeInMillis = ride.dateMillis
          rideCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
          rideCal.set(java.util.Calendar.MINUTE, 0)
          rideCal.set(java.util.Calendar.SECOND, 0)
          rideCal.set(java.util.Calendar.MILLISECOND, 0)
          
          val diff = todayMillis - rideCal.timeInMillis
          if (diff >= 0) {
              val dayDiff = (diff / oneDayMillis).toInt()
              if (dayDiff in 0..6) {
                  days[6 - dayDiff] += ride.distanceKm
              }
          }
      }
      return days.toList()
  }

  private fun calculateWeeklyTrend(history: List<RideSession>): List<Float> {
      val cal = java.util.Calendar.getInstance()
      cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
      cal.set(java.util.Calendar.MINUTE, 0)
      cal.set(java.util.Calendar.SECOND, 0)
      cal.set(java.util.Calendar.MILLISECOND, 0)
      val todayMillis = cal.timeInMillis
      val oneWeekMillis = 7 * 24 * 60 * 60 * 1000L
      
      val weeks = FloatArray(4)
      
      history.forEach { ride ->
          val rideCal = java.util.Calendar.getInstance()
          rideCal.timeInMillis = ride.dateMillis
          rideCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
          rideCal.set(java.util.Calendar.MINUTE, 0)
          rideCal.set(java.util.Calendar.SECOND, 0)
          rideCal.set(java.util.Calendar.MILLISECOND, 0)
          
          val diff = todayMillis - rideCal.timeInMillis
          if (diff >= 0) {
              val weekDiff = (diff / oneWeekMillis).toInt()
              if (weekDiff in 0..3) {
                  weeks[3 - weekDiff] += ride.distanceKm
              }
          }
      }
      return weeks.toList()
  }

  private fun loadUserProfile() {
      val uid = getCurrentUserIdUseCase() ?: return
      viewModelScope.launch {
          getUserProfileUseCase(uid).collect { result ->
             result.onSuccess { profile ->
                 userWeightKg = profile.weight ?: 70f
             }
          }
      }
  }

  fun selectBarn(barn: BarnWithLocation) {
      _uiState.update { it.copy(selectedBarn = barn) }
  }

  fun toggleRide() {
    viewModelScope.launch {
        if (_uiState.value.isRiding) {
            stopRideUseCase(_uiState.value.selectedBarn?.barn?.name)
        } else {
            startRideUseCase(userWeightKg)
        }
    }
  }

  fun setAutoDetect(enabled: Boolean) {
      viewModelScope.launch {
          setAutoDetectUseCase(enabled)
          _uiState.update { it.copy(autoDetect = enabled) }
      }
  }

  fun getAverageCalories(): Float {
      // This will be populated when history is loaded
      return 0f
  }

  fun getAverageCaloriesForRide(durationSec: Int, calories: Int): Float {
      return calculateAverageCaloriesUseCase(
          listOf(
              com.horsegallop.domain.ride.model.RideSession(
                  id = "current",
                  dateMillis = System.currentTimeMillis(),
                  durationSec = durationSec,
                  distanceKm = 0f,
                  calories = calories,
                  pathPoints = emptyList()
              )
          )
      )
  }
  
  fun getAverageSpeed(): Float {
      return calculateAverageSpeedUseCase(emptyList())
  }
  
  fun saveCurrentRide(barnName: String? = null) {
      viewModelScope.launch {
          stopRideUseCase(barnName)
      }
  }
}
