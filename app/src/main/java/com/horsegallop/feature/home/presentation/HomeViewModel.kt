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
              totalDistance = String.format(Locale.US, "%.1f", stats.totalDistance)
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
              title = activity.title,
              dateLabel = formatDate(activity.timestamp),
              timeLabel = formatTime(activity.timestamp),
              durationMin = activity.durationMin,
              distanceKm = activity.distanceKm
            )
          }
          _ui.update { it.copy(activities = items, loading = false) }
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
}

data class HomeUiState(
  val activities: List<ActivityUi> = emptyList(),
  val loading: Boolean = true,
  val error: String? = null,
  val totalRides: String = "0",
  val totalDistance: String = "0.0"
)

data class ActivityUi(
  val title: String,
  val dateLabel: String,
  val timeLabel: String,
  val durationMin: Int,
  val distanceKm: Double
)
