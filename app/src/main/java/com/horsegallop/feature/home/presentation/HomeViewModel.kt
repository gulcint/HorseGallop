package com.horsegallop.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.auth.usecase.GetCurrentUserIdUseCase
import com.horsegallop.domain.home.usecase.GetRecentActivitiesUseCase
import com.horsegallop.domain.home.usecase.GetUserStatsUseCase
import com.horsegallop.domain.content.usecase.GetAppContentUseCase
import com.horsegallop.domain.horse.model.HorseTip
import com.horsegallop.domain.horse.usecase.GetHorseTipsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
  private val getUserStatsUseCase: GetUserStatsUseCase,
  private val getAppContentUseCase: GetAppContentUseCase,
  private val getHorseTipsUseCase: GetHorseTipsUseCase
) : ViewModel() {

  private val _ui = MutableStateFlow(HomeUiState())
  val ui: StateFlow<HomeUiState> = _ui

  init {
    refresh()
  }

  fun refresh(limit: Int = 5) {
    loadDeferredNonCriticalContent()
    val uid = getCurrentUserIdUseCase()
    if (uid == null) {
      _ui.update {
        it.copy(
          loading = false,
          error = "User session not found",
          isEmpty = true
        )
      }
      return
    }

    _ui.update { it.copy(loading = true, error = null) }
    loadStats(uid)
    loadRecentActivities(uid, limit)
  }

  private fun loadDeferredNonCriticalContent() {
    viewModelScope.launch {
      delay(250)
      loadDynamicContent()
    }
    viewModelScope.launch {
      delay(400)
      loadHorseTips()
    }
  }

  private fun loadHorseTips() {
    val locale = Locale.getDefault().language
    viewModelScope.launch {
      getHorseTipsUseCase(locale).onSuccess { tips ->
        val selected = tips.randomOrNull()
        _ui.update { it.copy(currentTip = selected, allTips = tips) }
      }
      // Non-critical: ignore failure, static fallback will show
    }
  }

  private fun loadDynamicContent() {
    val locale = Locale.getDefault().language
    viewModelScope.launch {
      getAppContentUseCase(locale).collect { result ->
        result.onSuccess { content ->
          _ui.update {
            it.copy(
              heroTitle = content.homeHeroTitle ?: it.heroTitle,
              heroSubtitle = content.homeHeroSubtitle ?: it.heroSubtitle
            )
          }
        }
      }
    }
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
              favoriteBarn = stats.favoriteBarn ?: "-"
            )
          }
        }.onFailure { error ->
          // Backend not yet deployed or unavailable — fail silently with defaults
          com.horsegallop.core.debug.AppLog.w("HomeViewModel", "Stats unavailable: ${error.message}")
          _ui.update { it.copy(loading = false) }
        }
      }
    }
  }

  fun loadRecentActivities(limit: Int = 5) {
    val uid = getCurrentUserIdUseCase() ?: return
    loadRecentActivities(uid, limit)
  }

  private fun loadRecentActivities(uid: String, limit: Int) {
    viewModelScope.launch {
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
          val totalCount = items.size
          val distribution = if (totalCount > 0) {
            items.groupingBy { it.title ?: "Other" }
              .eachCount()
              .map { (title, count) -> title to (count.toFloat() / totalCount) }
              .sortedByDescending { it.second }
          } else {
            emptyList()
          }

          _ui.update {
            it.copy(
              activities = items,
              loading = false,
              error = if (items.isEmpty()) null else it.error,
              isEmpty = items.isEmpty(),
              activityDistribution = distribution,
              dailyDistance = calculateDailyDistance(activities)
            )
          }
        }.onFailure { e ->
          // Backend not yet deployed or unavailable — show empty state without error card
          com.horsegallop.core.debug.AppLog.w("HomeViewModel", "Activities unavailable: ${e.message}")
          _ui.update {
            it.copy(
              loading = false,
              isEmpty = true
            )
          }
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
      val ts = activity.timestamp ?: return@forEach
      val rideCal = java.util.Calendar.getInstance()
      rideCal.time = ts
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
  val isEmpty: Boolean = false,
  val heroTitle: String? = null,
  val heroSubtitle: String? = null,
  val totalRides: String = "0",
  val totalDistance: String = "0.0",
  val totalDuration: String = "0h 0m",
  val totalCalories: String = "0",
  val favoriteBarn: String = "-",
  val activityDistribution: List<Pair<String?, Float>> = emptyList(),
  val dailyDistance: List<Float> = emptyList(),
  val currentTip: HorseTip? = null,
  val allTips: List<HorseTip> = emptyList()
)

data class ActivityUi(
  val id: String,
  val title: String?,
  val dateLabel: String,
  val timeLabel: String,
  val durationMin: Int,
  val distanceKm: Double
)
