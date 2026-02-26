@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.horsegallop.feature.ride.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.horsegallop.domain.barn.model.BarnUi
import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.domain.ride.model.GeoPoint
import com.horsegallop.domain.ride.model.RideSyncStatus
import java.util.Locale

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

@Composable
fun RideTrackingScreen(
    viewModel: RideTrackingViewModel,
    onHomeClick: () -> Unit = {},
    onBarnsClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var hasLocationPermission by remember { mutableStateOf(context.hasLocationPermission()) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grantResult ->
        hasLocationPermission = grantResult.values.any { it }
    }

    LaunchedEffect(Unit) {
        hasLocationPermission = context.hasLocationPermission()
    }

    LaunchedEffect(state.errorMessageResId) {
        val messageRes = state.errorMessageResId ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(context.getString(messageRes))
        viewModel.clearError()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
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
    var showStopDialog by remember { mutableStateOf(false) }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text(text = stringResource(id = R.string.stop_ride_confirmation_title)) },
            text = { Text(text = stringResource(id = R.string.stop_ride_confirmation_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showStopDialog = false
                        onToggleRide()
                    }
                ) { Text(text = stringResource(id = R.string.finish_ride)) }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) {
                    Text(text = stringResource(id = R.string.action_cancel))
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            RideHeader(isRiding = state.isRiding)
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
                PermissionCard(
                    onGrantPermission = onRequestLocationPermission,
                    modifier = Modifier.testTag(RideTestTags.PermissionCard)
                )
            }
        }

        if (state.isRiding) {
            item {
                RideMapCard(
                    pathPoints = state.pathPoints,
                    elapsedSec = state.durationSec,
                    hasLocationPermission = hasLocationPermission
                )
            }
            item {
                MetricsGrid(state = state)
            }
            item {
                if (state.isSaving) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                LiveActions(
                    autoDetect = state.autoDetect,
                    onSetAutoDetect = onSetAutoDetect,
                    onFinishRide = { showStopDialog = true }
                )
            }
        } else {
            item {
                RideTypeSection(
                    selectedRideType = state.selectedRideType,
                    onRideTypeSelected = onRideTypeSelected
                )
            }
            item {
                BarnSelector(
                    barns = state.barns,
                    selectedBarn = state.selectedBarn,
                    onBarnSelected = onBarnSelected
                )
            }
            item {
                Button(
                    onClick = onToggleRide,
                    enabled = hasLocationPermission,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag(RideTestTags.StartButton),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.start_ride),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (!hasLocationPermission) {
                    OutlinedButton(
                        onClick = onRequestLocationPermission,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = stringResource(id = R.string.ride_grant_location))
                    }
                }
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

@Composable
private fun RideHeader(isRiding: Boolean) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f),
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
        )
    )
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(id = R.string.ride_live_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isRiding) {
                        stringResource(id = R.string.ride_live_subtitle_active)
                    } else {
                        stringResource(id = R.string.ride_live_subtitle_idle)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

    val statusText = when {
        pendingSyncCount > 0 -> stringResource(
            id = R.string.ride_sync_pending_count,
            pendingSyncCount
        )
        lastStatus == RideSyncStatus.Synced -> stringResource(id = R.string.ride_sync_completed)
        else -> stringResource(id = R.string.ride_sync_failed)
    }

    val containerColor = when {
        pendingSyncCount > 0 -> MaterialTheme.colorScheme.secondaryContainer
        lastStatus == RideSyncStatus.Synced -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(RideTestTags.SyncStatusCard),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
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
                    shape = RoundedCornerShape(14.dp)
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
    onGrantPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
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
                    contentDescription = null
                )
                Text(
                    text = stringResource(id = R.string.ride_permission_required),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = stringResource(id = R.string.ride_permission_hint),
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = onGrantPermission,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(text = stringResource(id = R.string.ride_grant_location))
            }
        }
    }
}

@Composable
private fun RideTypeSection(
    selectedRideType: RideType,
    onRideTypeSelected: (RideType) -> Unit
) {
    Card(
        modifier = Modifier.testTag(RideTestTags.RideTypeSection),
        shape = RoundedCornerShape(20.dp)
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
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RideType.values().drop(2).forEach { rideType ->
                    FilterChip(
                        selected = selectedRideType == rideType,
                        onClick = { onRideTypeSelected(rideType) },
                        label = { Text(text = stringResource(id = rideType.labelResId)) },
                        modifier = Modifier.weight(1f)
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
    var expanded by remember { mutableStateOf(false) }
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.select_barn),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedBarn?.barn?.name ?: stringResource(id = R.string.select_barn_hint),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth()
                        .testTag(RideTestTags.BarnField),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    barns.forEach { barn ->
                        DropdownMenuItem(
                            text = { Text(text = barn.barn.name) },
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

@Composable
private fun RideMapCard(
    pathPoints: List<GeoPoint>,
    elapsedSec: Int,
    hasLocationPermission: Boolean
) {
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                val polylinePoints = validPath.map { LatLng(it.latitude, it.longitude) }
                if (polylinePoints.size >= 2) {
                    Polyline(
                        points = polylinePoints,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                        width = 18f,
                        geodesic = true,
                        jointType = JointType.ROUND
                    )
                    Polyline(
                        points = polylinePoints,
                        color = MaterialTheme.colorScheme.primary,
                        width = 9f,
                        geodesic = true,
                        jointType = JointType.ROUND
                    )
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
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
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
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onFinishRide,
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .testTag(RideTestTags.FinishButton),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(
                text = stringResource(id = R.string.ride_finish_and_save),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onError
            )
        }
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.auto_ride_detection),
                    style = MaterialTheme.typography.labelMedium
                )
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
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
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
