@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.horsegallop.feature.ride.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.horsegallop.R
import com.horsegallop.core.feedback.LocalAppFeedbackController
import com.horsegallop.domain.barn.model.BarnUi
import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.domain.ride.model.GeoPoint
import com.horsegallop.domain.ride.model.RideSyncStatus
import com.horsegallop.ui.theme.LocalSemanticColors
import java.util.Locale

// ---------------------------------------------------------------------------
// Route entry point
// ---------------------------------------------------------------------------

@Composable
fun RideTrackingRoute(
    viewModel: RideTrackingViewModel = hiltViewModel(),
    onHomeClick: () -> Unit = {},
    onBarnsClick: () -> Unit = {}
) {
    RideTrackingScreen(
        viewModel = viewModel,
        onHomeClick = onHomeClick,
        onBarnsClick = onBarnsClick
    )
}

// ---------------------------------------------------------------------------
// Screen — permission wiring + dialog hosting (~90 satır)
// ---------------------------------------------------------------------------

@Composable
fun RideTrackingScreen(
    viewModel: RideTrackingViewModel,
    onHomeClick: () -> Unit = {},
    onBarnsClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val feedback = LocalAppFeedbackController.current
    val semantic = LocalSemanticColors.current

    var hasLocationPermission by remember { mutableStateOf(context.hasLocationPermission()) }
    var permissionRequestedOnce by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grantResult ->
        hasLocationPermission = grantResult.values.any { it }
    }

    LaunchedEffect(Unit) {
        hasLocationPermission = context.hasLocationPermission()
        if (!hasLocationPermission && !permissionRequestedOnce) {
            permissionRequestedOnce = true
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(state.errorMessageResId) {
        val messageRes = state.errorMessageResId ?: return@LaunchedEffect
        feedback.showError(messageRes)
        viewModel.clearError()
    }

    if (state.showAutoStopDialog) {
        RideAutoStopDialog(
            onConfirm = viewModel::confirmAutoStop,
            onDismiss = viewModel::dismissAutoStopDialog
        )
    }

    Scaffold(containerColor = semantic.screenBase) { innerPadding ->
        RideTrackingContent(
            state = state,
            hasLocationPermission = hasLocationPermission,
            onRequestLocationPermission = {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            onToggleRide = { viewModel.onToggleRide(hasLocationPermission) },
            onSetAutoDetect = viewModel::onSetAutoDetect,
            onBarnSelected = viewModel::onBarnSelected,
            onRideTypeSelected = viewModel::onRideTypeSelected,
            onDismissSavedSummary = viewModel::dismissSavedSummary,
            onRetryPendingSync = viewModel::onRetryPendingSync,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// Dialogs
// ---------------------------------------------------------------------------

@Composable
private fun RideAutoStopDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.ride_auto_stop_title)) },
        text = { Text(text = stringResource(R.string.ride_auto_stop_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.ride_auto_stop_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.ride_auto_stop_dismiss))
            }
        }
    )
}

@Composable
private fun RideStopConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.stop_ride_confirmation_title)) },
        text = { Text(text = stringResource(id = R.string.stop_ride_confirmation_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.finish_ride))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.action_cancel))
            }
        }
    )
}

// ---------------------------------------------------------------------------
// Content — LazyColumn scaffold (~70 satır)
// ---------------------------------------------------------------------------

@Composable
fun RideTrackingContent(
    state: RideUiState,
    hasLocationPermission: Boolean,
    onRequestLocationPermission: () -> Unit,
    onToggleRide: () -> Unit,
    onSetAutoDetect: (Boolean) -> Unit,
    onBarnSelected: (BarnWithLocation) -> Unit,
    onRideTypeSelected: (RideType) -> Unit,
    onDismissSavedSummary: () -> Unit,
    onRetryPendingSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    var showStopDialog by remember { mutableStateOf(false) }

    if (showStopDialog) {
        RideStopConfirmDialog(
            onConfirm = {
                showStopDialog = false
                onToggleRide()
            },
            onDismiss = { showStopDialog = false }
        )
    }

    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    semantic.screenTopBar.copy(alpha = 0.34f),
                    semantic.screenBase
                )
            )
        )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                RideHeader(
                    isRiding = state.isRiding,
                    liveTitle = state.liveTitle,
                    liveSubtitleIdle = state.liveSubtitleIdle,
                    liveSubtitleActive = state.liveSubtitleActive
                )
            }
            item {
                SyncStatusCard(
                    pendingSyncCount = state.pendingSyncCount,
                    lastStatus = state.lastStopSyncStatus,
                    isRetrying = state.isRetryingSync,
                    onRetryPendingSync = onRetryPendingSync
                )
            }
            if (!hasLocationPermission) {
                item {
                    RideTrackingPermissionRequest(
                        title = state.permissionTitle,
                        hint = state.permissionHint,
                        grantCta = state.grantLocationCta,
                        onGrantPermission = onRequestLocationPermission
                    )
                }
            }
            if (state.isRiding) {
                item {
                    RideTrackingActiveSection(
                        state = state,
                        hasLocationPermission = hasLocationPermission,
                        autoDetect = state.autoDetect,
                        onSetAutoDetect = onSetAutoDetect,
                        onFinishRide = { showStopDialog = true }
                    )
                }
            } else {
                item {
                    RideTrackingIdleSection(
                        state = state,
                        hasLocationPermission = hasLocationPermission,
                        onRideTypeSelected = onRideTypeSelected,
                        onBarnSelected = onBarnSelected,
                        onStartRide = onToggleRide
                    )
                }
            }
            state.savedRideSummary?.let { summary ->
                item {
                    SavedRideSummaryCard(
                        summary = summary,
                        onDismiss = onDismissSavedSummary,
                        modifier = Modifier.testTag(RideTestTags.SavedSummaryCard)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Active ride section — map + metrics + controls
// ---------------------------------------------------------------------------

@Composable
private fun RideTrackingActiveSection(
    state: RideUiState,
    hasLocationPermission: Boolean,
    autoDetect: Boolean,
    onSetAutoDetect: (Boolean) -> Unit,
    onFinishRide: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RideTrackingMapSection(
            pathPoints = state.pathPoints,
            elapsedSec = state.durationSec,
            hasLocationPermission = hasLocationPermission
        )
        RideTrackingMetricsBar(state = state)
        if (state.isSaving) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        RideTrackingControls(
            autoDetect = autoDetect,
            onSetAutoDetect = onSetAutoDetect,
            onFinishRide = onFinishRide
        )
    }
}

// ---------------------------------------------------------------------------
// Idle (pre-ride) section — type selector + barn + start button
// ---------------------------------------------------------------------------

@Composable
private fun RideTrackingIdleSection(
    state: RideUiState,
    hasLocationPermission: Boolean,
    onRideTypeSelected: (RideType) -> Unit,
    onBarnSelected: (BarnWithLocation) -> Unit,
    onStartRide: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RideTypeSection(
            selectedRideType = state.selectedRideType,
            onRideTypeSelected = onRideTypeSelected
        )
        RideTrackingBarnSelector(
            barns = state.barns,
            selectedBarn = state.selectedBarn,
            onBarnSelected = onBarnSelected
        )
        RideTrackingStartButton(
            canStart = hasLocationPermission && state.selectedBarn != null,
            onStartRide = onStartRide
        )
    }
}

// ---------------------------------------------------------------------------
// Named sub-composables (public aliases for extracted sections)
// ---------------------------------------------------------------------------

/** Map card with gait polyline, legend and elapsed timer overlay. */
@Composable
fun RideTrackingMapSection(
    pathPoints: List<GeoPoint>,
    elapsedSec: Int,
    hasLocationPermission: Boolean
) {
    RideMapCard(
        pathPoints = pathPoints,
        elapsedSec = elapsedSec,
        hasLocationPermission = hasLocationPermission
    )
}

/** Speed / distance / calories / altitude metrics grid. */
@Composable
fun RideTrackingMetricsBar(state: RideUiState) {
    MetricsGrid(state = state)
}

/** Finish button + auto-detect toggle card. */
@Composable
fun RideTrackingControls(
    autoDetect: Boolean,
    onSetAutoDetect: (Boolean) -> Unit,
    onFinishRide: () -> Unit
) {
    LiveActions(
        autoDetect = autoDetect,
        onSetAutoDetect = onSetAutoDetect,
        onFinishRide = onFinishRide
    )
}

/** Barn dropdown selector card. */
@Composable
fun RideTrackingBarnSelector(
    barns: List<BarnWithLocation>,
    selectedBarn: BarnWithLocation?,
    onBarnSelected: (BarnWithLocation) -> Unit
) {
    BarnSelector(
        barns = barns,
        selectedBarn = selectedBarn,
        onBarnSelected = onBarnSelected
    )
}

/** Location permission required card. */
@Composable
fun RideTrackingPermissionRequest(
    title: String?,
    hint: String?,
    grantCta: String?,
    onGrantPermission: () -> Unit
) {
    PermissionCard(
        title = title,
        hint = hint,
        grantCta = grantCta,
        onGrantPermission = onGrantPermission,
        modifier = Modifier.testTag(RideTestTags.PermissionCard)
    )
}

/** Current gait badge shown during active ride. */
@Composable
fun RideGaitIndicator(gait: String) {
    val semantic = LocalSemanticColors.current
    val gaitColor = when (gait) {
        "trot"   -> semantic.gaitTrot
        "canter" -> semantic.gaitCanter
        else     -> semantic.gaitWalk
    }
    val labelRes = when (gait) {
        "trot"   -> R.string.gait_trot
        "canter" -> R.string.gait_canter
        else     -> R.string.gait_walk
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(gaitColor.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(gaitColor)
            )
            Text(
                text = stringResource(id = labelRes),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Start button (pulse animation)
// ---------------------------------------------------------------------------

@Composable
private fun RideTrackingStartButton(
    canStart: Boolean,
    onStartRide: () -> Unit
) {
    val pulseTransition = rememberInfiniteTransition(label = "start_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (canStart) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    Button(
        onClick = onStartRide,
        enabled = canStart,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(if (canStart) pulseScale else 1f)
            .testTag(RideTestTags.StartButton),
        shape = RoundedCornerShape(18.dp)
    ) {
        Icon(
            Icons.AutoMirrored.Filled.DirectionsRun,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = stringResource(id = R.string.start_ride),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

// ---------------------------------------------------------------------------
// Existing private composables (unchanged logic, kept in same file)
// ---------------------------------------------------------------------------

@Composable
private fun RideHeader(
    isRiding: Boolean,
    liveTitle: String?,
    liveSubtitleIdle: String?,
    liveSubtitleActive: String?
) {
    val semantic = LocalSemanticColors.current
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
            semantic.cardElevated
        )
    )
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = liveTitle ?: stringResource(id = R.string.ride_live_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isRiding) {
                        liveSubtitleActive ?: stringResource(id = R.string.ride_live_subtitle_active)
                    } else {
                        liveSubtitleIdle ?: stringResource(id = R.string.ride_live_subtitle_idle)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (isRiding) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            } else {
                                semantic.cardSubtle
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isRiding) {
                            stringResource(id = R.string.finish_ride)
                        } else {
                            stringResource(id = R.string.start_ride)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncStatusCard(
    pendingSyncCount: Int,
    lastStatus: RideSyncStatus?,
    isRetrying: Boolean,
    onRetryPendingSync: () -> Unit
) {
    if (pendingSyncCount <= 0 && lastStatus == null) return
    val semantic = LocalSemanticColors.current

    val statusText = when {
        pendingSyncCount > 0 -> stringResource(
            id = R.string.ride_sync_pending_count,
            pendingSyncCount
        )
        lastStatus == RideSyncStatus.Synced -> stringResource(id = R.string.ride_sync_completed)
        else -> stringResource(id = R.string.ride_sync_failed)
    }

    val containerColor = when {
        pendingSyncCount > 0 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.62f)
        lastStatus == RideSyncStatus.Synced -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.58f)
        else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.52f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(RideTestTags.SyncStatusCard),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (pendingSyncCount > 0) {
                OutlinedButton(
                    onClick = onRetryPendingSync,
                    enabled = !isRetrying,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(RideTestTags.RetrySyncButton),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, semantic.cardStroke)
                ) {
                    Text(
                        text = if (isRetrying) {
                            stringResource(id = R.string.loading)
                        } else {
                            stringResource(id = R.string.ride_sync_retry)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String?,
    hint: String?,
    grantCta: String?,
    onGrantPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardSubtle),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = title ?: stringResource(id = R.string.ride_permission_required),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = hint ?: stringResource(id = R.string.ride_permission_hint),
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = onGrantPermission,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(text = grantCta ?: stringResource(id = R.string.ride_grant_location))
            }
        }
    }
}

@Composable
private fun RideTypeSection(
    selectedRideType: RideType,
    onRideTypeSelected: (RideType) -> Unit
) {
    val semantic = LocalSemanticColors.current
    Card(
        modifier = Modifier.testTag(RideTestTags.RideTypeSection),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(id = R.string.ride_type_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(id = R.string.ride_type_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RideType.values().take(2).forEach { rideType ->
                    FilterChip(
                        selected = selectedRideType == rideType,
                        onClick = { onRideTypeSelected(rideType) },
                        label = { Text(text = stringResource(id = rideType.labelResId)) },
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = semantic.chipSelected,
                            containerColor = semantic.chipUnselected
                        )
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RideType.values().drop(2).forEach { rideType ->
                    FilterChip(
                        selected = selectedRideType == rideType,
                        onClick = { onRideTypeSelected(rideType) },
                        label = { Text(text = stringResource(id = rideType.labelResId)) },
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = semantic.chipSelected,
                            containerColor = semantic.chipUnselected
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun BarnSelector(
    barns: List<BarnWithLocation>,
    selectedBarn: BarnWithLocation?,
    onBarnSelected: (BarnWithLocation) -> Unit
) {
    val semantic = LocalSemanticColors.current
    var expanded by remember { mutableStateOf(false) }
    val selectedBarnName = selectedBarn?.barn?.name
    val selectedBarnDescription = selectedBarn?.barn?.description?.takeIf { it.isNotBlank() }
        ?: selectedBarn?.barn?.location?.takeIf { it.isNotBlank() }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.select_barn),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                selectedBarnName?.let {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = stringResource(id = R.string.selected_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            Text(
                text = stringResource(id = R.string.ride_select_barn_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                Surface(
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth()
                        .testTag(RideTestTags.BarnField)
                        .clickable { expanded = !expanded },
                    shape = RoundedCornerShape(16.dp),
                    color = semantic.cardSubtle,
                    border = androidx.compose.foundation.BorderStroke(1.dp, semantic.cardStroke)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = selectedBarnName ?: stringResource(id = R.string.select_barn_hint),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = selectedBarnDescription
                                    ?: stringResource(id = R.string.ride_select_barn_support_text),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = semantic.panelOverlay
                ) {
                    if (barns.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.select_barn_empty)) },
                            onClick = {},
                            enabled = false
                        )
                    } else {
                        barns.forEach { barn ->
                            val isSelected = selectedBarn?.barn?.id == barn.barn.id
                            DropdownMenuItem(
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = barn.barn.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                        (barn.barn.description.takeIf { it.isNotBlank() }
                                            ?: barn.barn.location.takeIf { it.isNotBlank() })?.let { description ->
                                            Text(
                                                text = description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    onBarnSelected(barn)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RideMapCard(
    pathPoints: List<GeoPoint>,
    elapsedSec: Int,
    hasLocationPermission: Boolean
) {
    val semantic = LocalSemanticColors.current
    val validPath = remember(pathPoints) {
        pathPoints
            .filter { it.latitude != 0.0 || it.longitude != 0.0 }
            .distinctBy { "${it.latitude}:${it.longitude}" }
    }
    val defaultLocation = LatLng(41.0082, 28.9784)
    val focusedPoint = validPath.lastOrNull()?.let { LatLng(it.latitude, it.longitude) } ?: defaultLocation
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(focusedPoint, 15f)
    }

    LaunchedEffect(validPath.size) {
        if (validPath.isEmpty()) return@LaunchedEffect
        if (validPath.size == 1) {
            val point = validPath.first()
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(point.latitude, point.longitude), 15f)
            )
            return@LaunchedEffect
        }
        // Throttle camera updates to avoid jank when GPS updates frequently.
        if (validPath.size % 5 != 0) return@LaunchedEffect
        val boundsBuilder = LatLngBounds.Builder()
        validPath.forEach { boundsBuilder.include(LatLng(it.latitude, it.longitude)) }
        runCatching {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 90)
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.6f)
            .testTag(RideTestTags.MapCard),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, semantic.cardStroke),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = hasLocationPermission
                )
            ) {
                val gaitSegments = remember(validPath.size) { buildGaitSegments(validPath) }
                gaitSegments.forEach { (segPoints, gait) ->
                    val segColor = when (gait) {
                        "trot"   -> semantic.gaitTrot
                        "canter" -> semantic.gaitCanter
                        else     -> semantic.gaitWalk
                    }
                    if (segPoints.size >= 2) {
                        Polyline(points = segPoints, color = segColor.copy(alpha = 0.28f), width = 18f, geodesic = true, jointType = JointType.ROUND)
                        Polyline(points = segPoints, color = segColor, width = 9f, geodesic = true, jointType = JointType.ROUND)
                    }
                }
                validPath.firstOrNull()?.let { start ->
                    Marker(
                        state = MarkerState(position = LatLng(start.latitude, start.longitude)),
                        title = stringResource(id = R.string.map_start),
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                }
                validPath.lastOrNull()?.let { end ->
                    Marker(
                        state = MarkerState(position = LatLng(end.latitude, end.longitude)),
                        title = stringResource(id = R.string.map_end),
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 12.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(semantic.panelOverlay.copy(alpha = 0.95f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.ride_live_title),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 10.dp, top = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(semantic.panelOverlay.copy(alpha = 0.90f))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GaitLegendDot(color = semantic.gaitWalk, label = stringResource(R.string.gait_walk))
                GaitLegendDot(color = semantic.gaitTrot, label = stringResource(R.string.gait_trot))
                GaitLegendDot(color = semantic.gaitCanter, label = stringResource(R.string.gait_canter))
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp)
                    .clip(CircleShape)
                    .background(semantic.panelOverlay.copy(alpha = 0.92f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = formatDuration(elapsedSec),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun MetricsGrid(state: RideUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.AccessTime,
                label = stringResource(id = R.string.stat_duration),
                value = formatDuration(state.durationSec),
                unit = ""
            )
            MetricTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Speed,
                label = stringResource(id = R.string.ride_avg_speed),
                value = String.format(Locale.US, "%.1f", state.avgSpeedKmh),
                unit = stringResource(id = R.string.unit_kmh)
            )
            MetricTile(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Filled.DirectionsRun,
                label = stringResource(id = R.string.label_speed),
                value = String.format(Locale.US, "%.1f", state.speedKmh),
                unit = stringResource(id = R.string.unit_kmh)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricTile(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                label = stringResource(id = R.string.ride_max_speed),
                value = String.format(Locale.US, "%.1f", state.maxSpeedKmh),
                unit = stringResource(id = R.string.unit_kmh)
            )
            MetricTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Straighten,
                label = stringResource(id = R.string.label_distance),
                value = String.format(Locale.US, "%.2f", state.distanceKm),
                unit = stringResource(id = R.string.unit_km)
            )
            MetricTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.LocalFireDepartment,
                label = stringResource(id = R.string.metric_calories),
                value = state.calories.toString(),
                unit = stringResource(id = R.string.unit_kcal)
            )
        }
        if (state.isRiding) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.LocationOn,
                    label = stringResource(id = R.string.metric_altitude),
                    value = String.format(Locale.US, "%.0f", state.altitudeM),
                    unit = stringResource(id = R.string.unit_m)
                )
                MetricTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.LocalFireDepartment,
                    label = stringResource(id = R.string.metric_horse_calories),
                    value = state.horseCalories.toString(),
                    unit = stringResource(id = R.string.unit_kcal)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricTile(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    unit: String
) {
    val semantic = LocalSemanticColors.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (unit.isNotBlank()) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveActions(
    autoDetect: Boolean,
    onSetAutoDetect: (Boolean) -> Unit,
    onFinishRide: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    val autoDetectOnHint = stringResource(id = R.string.auto_detect_on_hint)
    val autoDetectOffHint = stringResource(id = R.string.auto_detect_off_hint)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = onFinishRide,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag(RideTestTags.FinishButton),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = stringResource(id = R.string.ride_finish_and_save),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, semantic.cardStroke)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.auto_ride_detection),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (autoDetect) autoDetectOnHint else autoDetectOffHint,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Switch(
                    checked = autoDetect,
                    onCheckedChange = onSetAutoDetect
                )
            }
        }
    }
}

@Composable
private fun SavedRideSummaryCard(
    summary: SavedRideSummary,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardSubtle),
        border = androidx.compose.foundation.BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.ride_saved_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(id = R.string.ride_saved_subtitle),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${formatDuration(summary.durationSec)} • ${String.format(Locale.US, "%.2f", summary.distanceKm)} ${stringResource(id = R.string.unit_km)} • ${summary.calories} ${stringResource(id = R.string.unit_kcal)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${stringResource(id = R.string.ride_avg_speed)} ${String.format(Locale.US, "%.1f", summary.avgSpeedKmh)} ${stringResource(id = R.string.unit_kmh)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${stringResource(id = R.string.ride_max_speed)} ${String.format(Locale.US, "%.1f", summary.maxSpeedKmh)} ${stringResource(id = R.string.unit_kmh)}",
                style = MaterialTheme.typography.bodyMedium
            )
            summary.rideType?.let {
                Text(
                    text = stringResource(id = it.labelResId),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            summary.barnName?.takeIf { it.isNotBlank() }?.let {
                Text(text = stringResource(id = R.string.riding_at, it))
            }
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    }
}

@Composable
private fun GaitLegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

// ---------------------------------------------------------------------------
// Pure helpers
// ---------------------------------------------------------------------------

private fun formatDuration(durationSec: Int): String {
    val hours = durationSec / 3600
    val minutes = (durationSec % 3600) / 60
    val seconds = durationSec % 60
    return if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

private fun Context.hasLocationPermission(): Boolean {
    val fineGranted = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fineGranted || coarseGranted
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true)
@Composable
private fun RideTrackingContentPreview() {
    RideTrackingContent(
        state = RideUiState(
            isRiding = false,
            barns = listOf(
                BarnWithLocation(
                    barn = BarnUi(
                        id = "1",
                        name = "Caddebostan Equestrian",
                        description = "Stable"
                    ),
                    lat = 41.0,
                    lng = 29.0,
                    amenities = emptySet()
                )
            )
        ),
        hasLocationPermission = true,
        onRequestLocationPermission = {},
        onToggleRide = {},
        onSetAutoDetect = {},
        onBarnSelected = {},
        onRideTypeSelected = {},
        onDismissSavedSummary = {},
        onRetryPendingSync = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun RideTrackingActiveSectionPreview() {
    RideTrackingActiveSection(
        state = RideUiState(
            isRiding = true,
            durationSec = 1245,
            speedKmh = 12.3f,
            avgSpeedKmh = 10.5f,
            maxSpeedKmh = 18.7f,
            distanceKm = 3.21f,
            calories = 142,
            horseCalories = 850,
            altitudeM = 34.0f
        ),
        hasLocationPermission = true,
        autoDetect = true,
        onSetAutoDetect = {},
        onFinishRide = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun RideGaitIndicatorPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
        RideGaitIndicator(gait = "walk")
        RideGaitIndicator(gait = "trot")
        RideGaitIndicator(gait = "canter")
    }
}

@Preview(showBackground = true)
@Composable
private fun RideAutoStopDialogPreview() {
    RideAutoStopDialog(onConfirm = {}, onDismiss = {})
}
