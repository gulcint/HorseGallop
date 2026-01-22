package com.horsegallop.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.auth.usecase.GetCurrentUserIdUseCase
import com.horsegallop.domain.home.usecase.GetRecentActivitiesUseCase
import com.horsegallop.domain.home.usecase.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
  private val getRecentActivitiesUseCase: GetRecentActivitiesUseCase,
  private val getUserStatsUseCase: GetUserStatsUseCase
) : ViewModel() {

  private val _ui = MutableStateFlow(HomeUiState())
  val ui: StateFlow<HomeUiState> = _ui

  init {
    loadData()
  }

  private fun loadData() {
    val uid = getCurrentUserIdUseCase()
    if (uid == null) {
      _ui.update { it.copy(loading = false) }
      return
    }

    loadStats(uid)
    loadRecentActivities()
  }

  private fun loadStats(uid: String) {
    viewModelScope.launch {
      getUserStatsUseCase(uid).collect { result ->
        result.onSuccess { stats ->
          _ui.update {
            it.copy(
              totalRides = stats.totalRides.toString(),
              totalDistance = String.format(Locale.US, "%.1f", stats.totalDistance),
              totalDuration = "${stats.totalDurationMin / 60}h ${stats.totalDurationMin % 60}m",
              totalCalories = stats.totalCalories.toInt().toString(),
              favoriteBarn = stats.favoriteBarn ?: "Unknown"
            )
          }
        }.onFailure {
          // Log error or handle silently for stats
        }
      }
    }
  }

  fun loadRecentActivities(limit: Int = 5) {
    val uid = getCurrentUserIdUseCase() ?: return
    viewModelScope.launch {
      _ui.update { it.copy(loading = true, error = null) }
      getRecentActivitiesUseCase(uid, limit).collect { result ->
        result.onSuccess { activities ->
          val items = activities.map { activity ->
            ActivityUi(
              id = activity.id,
              title = activity.title,
              dateLabel = formatDate(activity.timestamp),
              timeLabel = formatTime(activity.timestamp),
              durationMin = activity.durationMin,
              distanceKm = activity.distanceKm
            )
          }
          val totalCount = activities.size
          val distribution = if (totalCount > 0) {
            items.groupingBy { it.title ?: "Other" }
              .eachCount()
              .map { (title, count) ->
                title to (count.toFloat() / totalCount)
              }
              .sortedByDescending { it.second }
          } else {
            emptyList()
          }

          val dailyDistance = calculateDailyDistance(activities)

          _ui.update {
            it.copy(
              activities = items,
              loading = false,
              activityDistribution = distribution,
              dailyDistance = dailyDistance
            )
          }
        }.onFailure { e ->
          _ui.update { it.copy(loading = false, error = e.localizedMessage) }
        }
      }
    }
  }

  private fun formatDate(date: Date?): String {
    if (date == null) return ""
    val locale = Locale.getDefault()
    return SimpleDateFormat("MMM d", locale).format(date)
  }

  private fun formatTime(date: Date?): String {
    if (date == null) return ""
    val locale = Locale.getDefault()
    return SimpleDateFormat("HH:mm", locale).format(date)
  }

  private fun calculateDailyDistance(activities: List<com.horsegallop.domain.home.model.RideSession>): List<Float> {
    val cal = java.util.Calendar.getInstance()
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    val todayMillis = cal.timeInMillis
    val oneDayMillis = 24 * 60 * 60 * 1000L
    
    val days = FloatArray(7)
    
    activities.forEach { activity ->
        if (activity.timestamp == null) return@forEach
        val rideCal = java.util.Calendar.getInstance()
        rideCal.time = activity.timestamp!!
        rideCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        rideCal.set(java.util.Calendar.MINUTE, 0)
        rideCal.set(java.util.Calendar.SECOND, 0)
        rideCal.set(java.util.Calendar.MILLISECOND, 0)
        
        val diff = todayMillis - rideCal.timeInMillis
        if (diff >= 0) {
            val dayDiff = (diff / oneDayMillis).toInt()
            if (dayDiff in 0..6) {
                days[6 - dayDiff] += activity.distanceKm.toFloat()
            }
        }
    }
    return days.toList()
  }
}

data class HomeUiState(
  val activities: List<ActivityUi> = emptyList(),
  val loading: Boolean = true,
  val error: String? = null,
  val totalRides: String = "0",
  val totalDistance: String = "0.0",
  val totalDuration: String = "0h 0m",
  val totalCalories: String = "0",
  val favoriteBarn: String = "Unknown",
  val activityDistribution: List<Pair<String?, Float>> = emptyList(),
  val dailyDistance: List<Float> = emptyList()
)

data class ActivityUi(
  val id: String,
  val title: String?,
  val dateLabel: String,
  val timeLabel: String,
  val durationMin: Int,
  val distanceKm: Double
)
