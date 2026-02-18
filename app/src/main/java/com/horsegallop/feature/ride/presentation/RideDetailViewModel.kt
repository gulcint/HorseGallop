package com.horsegallop.feature.ride.presentation

import android.content.Context
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

    fun shareRouteOnly(context: Context) {
        val ride = _uiState.value.ride ?: return
        shareRideData(context, ride, ShareType.ROUTE_ONLY)
    }

    fun shareRouteWithStats(context: Context) {
        val ride = _uiState.value.ride ?: return
        shareRideData(context, ride, ShareType.ROUTE_WITH_STATS)
    }

    fun shareFullReport(context: Context) {
        val ride = _uiState.value.ride ?: return
        shareRideData(context, ride, ShareType.FULL_RIDE_REPORT)
    }

    private fun shareRideData(context: Context, ride: RideSession, shareType: ShareType) {
        val duration = formatDuration(ride.durationSec)
        val avgSpeed = if (ride.durationSec > 0) {
            (ride.distanceKm / (ride.durationSec / 3600f)).coerceAtLeast(0.001f)
        } else {
            0f
        }

        val shareMessage = when (shareType) {
            ShareType.ROUTE_ONLY -> 
                "İşte biniş rotam! 🐎\nHaritayı incele ve benimle aynı yolu takip et."
            
            ShareType.ROUTE_WITH_STATS -> 
                "Binişim tamamlandı! 🏇\n" +
                "• Mesafe: ${"%.2f".format(ride.distanceKm)} km\n" +
                "• Süre: $duration\n" +
                "• Ortalama Hız: ${"%.1f".format(avgSpeed)} km/h\n" +
                "Haritada rotamı görün!"
            
            ShareType.FULL_RIDE_REPORT -> 
                "Binişim Raporu! 🎯\n" +
                "• Tarih: ${formatDate(ride.dateMillis)}\n" +
                "• Mesafe: ${"%.2f".format(ride.distanceKm)} km\n" +
                "• Süre: $duration\n" +
                "• Ortalama Hız: ${"%.1f".format(avgSpeed)} km/h\n" +
                "• Yakılan Kalori: ${ride.calories} kcal\n" +
                "${if (!ride.barnName.isNullOrEmpty()) "• location: ${ride.barnName}\n" else ""}" +
                "Tüm detayları paylaşıyorum!"
        }

        val shareTitle = when (shareType) {
            ShareType.ROUTE_ONLY -> "Biniş Rotası"
            ShareType.ROUTE_WITH_STATS -> "Biniş İstatistikleri"
            ShareType.FULL_RIDE_REPORT -> "Tam Biniş Raporu"
        }

        val sendIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
        }

        val shareIntent = android.content.Intent.createChooser(sendIntent, shareTitle)
        context.startActivity(shareIntent)
    }

    private fun formatDate(millis: Long): String {
        val df = java.text.SimpleDateFormat("dd MMM yyyy HH:mm", java.util.Locale.getDefault())
        return df.format(java.util.Date(millis))
    }

    private fun formatDuration(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) {
            "%d:%02d:%02d".format(h, m, s)
        } else {
            "%d:%02d".format(m, s)
        }
    }

    enum class ShareType {
        ROUTE_ONLY,
        ROUTE_WITH_STATS,
        FULL_RIDE_REPORT
    }
}
