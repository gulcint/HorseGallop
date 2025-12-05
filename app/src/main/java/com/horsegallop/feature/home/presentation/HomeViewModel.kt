package com.horsegallop.feature.home.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val auth: FirebaseAuth,
  private val firestore: FirebaseFirestore
) : ViewModel() {

  data class UiState(
    val activities: List<ActivityUi> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
  )

  private val _ui = MutableStateFlow(UiState())
  val ui: StateFlow<UiState> = _ui

  init {
    loadRecentActivities()
  }

  fun loadRecentActivities() {
    val uid = auth.currentUser?.uid
    if (uid == null) {
      _ui.value = UiState(activities = emptyList(), loading = false, error = null)
      return
    }
    firestore.collection("users").document(uid).collection("rides")
      .orderBy("timestamp", Query.Direction.DESCENDING)
      .limit(2)
      .get()
      .addOnSuccessListener { qs ->
        val items = qs.documents.map { doc ->
          val ts = doc.getTimestamp("timestamp")?.toDate()
          val title = doc.getString("title") ?: "Ride"
          val durationMin = (doc.getLong("durationMin") ?: 0L).toInt()
          val distanceKm = doc.getDouble("distanceKm") ?: (doc.getLong("distanceKm")?.toDouble() ?: 0.0)
          val dateLabel = formatDate(ts)
          val timeLabel = formatTime(ts)
          ActivityUi(
            title = title,
            dateLabel = dateLabel,
            timeLabel = timeLabel,
            durationMin = durationMin,
            distanceKm = distanceKm
          )
        }
        _ui.value = UiState(activities = items, loading = false, error = null)
      }
      .addOnFailureListener { e ->
        _ui.value = UiState(activities = emptyList(), loading = false, error = e.localizedMessage)
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

