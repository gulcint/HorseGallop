package com.horsegallop.feature.ride.presentation

import androidx.annotation.StringRes
import com.horsegallop.R
import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.domain.ride.model.GeoPoint
import com.horsegallop.domain.ride.model.RideSyncStatus

enum class RideType(
    val backendValue: String,
    @StringRes val labelResId: Int
) {
    DRESSAGE("dressage", R.string.ride_type_dressage),
    SHOW_JUMPING("show_jumping", R.string.ride_type_show_jumping),
    ENDURANCE("endurance", R.string.ride_type_endurance),
    TRAIL_RIDING("trail_riding", R.string.ride_type_trail_riding)
}

data class SavedRideSummary(
    val durationSec: Int,
    val distanceKm: Float,
    val calories: Int,
    val avgSpeedKmh: Float,
    val maxSpeedKmh: Float,
    val rideType: RideType?,
    val barnName: String?,
    val savedAtMillis: Long
)

data class RideUiState(
    val speedKmh: Float = 0f,
    val avgSpeedKmh: Float = 0f,
    val maxSpeedKmh: Float = 0f,
    val distanceKm: Float = 0f,
    val durationSec: Int = 0,
    val calories: Int = 0,
    val horseCalories: Int = 0,
    val altitudeM: Float = 0f,
    val isRiding: Boolean = false,
    val autoDetect: Boolean = false,
    val showAutoStopDialog: Boolean = false,
    val showSafetyAlarmDialog: Boolean = false,
    val safetyEnabled: Boolean = false,
    val pathPoints: List<GeoPoint> = emptyList(),
    val barns: List<BarnWithLocation> = emptyList(),
    val selectedBarn: BarnWithLocation? = null,
    val selectedRideType: RideType = RideType.TRAIL_RIDING,
    val isSaving: Boolean = false,
    val isRetryingSync: Boolean = false,
    val savedRideSummary: SavedRideSummary? = null,
    val pendingSyncCount: Int = 0,
    val lastStopSyncStatus: RideSyncStatus? = null,
    val liveTitle: String? = null,
    val liveSubtitleIdle: String? = null,
    val liveSubtitleActive: String? = null,
    val permissionTitle: String? = null,
    val permissionHint: String? = null,
    val grantLocationCta: String? = null,
    @StringRes val errorMessageResId: Int? = null
)

object RideTestTags {
    const val RideTypeSection = "ride_type_section"
    const val BarnField = "ride_barn_field"
    const val PermissionCard = "ride_permission_card"
    const val MapCard = "ride_map_card"
    const val StartButton = "ride_start_button"
    const val FinishButton = "ride_finish_button"
    const val SavedSummaryCard = "ride_saved_summary_card"
    const val SyncStatusCard = "ride_sync_status_card"
    const val RetrySyncButton = "ride_retry_sync_button"
}
