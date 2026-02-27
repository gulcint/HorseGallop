package com.horsegallop.feature.ride.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.horsegallop.R
import com.horsegallop.domain.ride.model.RideSession
import com.horsegallop.ui.theme.LocalSemanticColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideDetailScreen(
    onBack: () -> Unit,
    viewModel: RideDetailViewModel = hiltViewModel()
) {
    val semantic = LocalSemanticColors.current
    val uiState by viewModel.uiState.collectAsState()
    val ride = uiState.ride

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ride_details_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = semantic.screenTopBar,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            ride != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        RideDetailHeroCard(ride = ride)
                    }
                    item {
                        RideDetailMapCard(ride = ride)
                    }
                    item {
                        RideStatsCard(ride = ride)
                    }
                    if (!ride.rideType.isNullOrBlank()) {
                        item {
                            InfoCard(
                                text = stringResource(
                                    id = R.string.ride_type_saved_format,
                                    rideTypeLabel(ride.rideType)
                                ),
                                containerBrush = Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
                                        semantic.cardElevated
                                    )
                                )
                            )
                        }
                    }
                    if (!ride.barnName.isNullOrBlank()) {
                        item {
                            InfoCard(
                                text = stringResource(R.string.riding_at, ride.barnName ?: ""),
                                containerBrush = Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
                                        semantic.cardElevated
                                    )
                                )
                            )
                        }
                    }
                }
            }

            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.ride_not_found))
                }
            }
        }
    }
}

@Composable
private fun RideDetailHeroCard(ride: RideSession) {
    val semantic = LocalSemanticColors.current
    val date = remember(ride.dateMillis) { Date(ride.dateMillis) }
    val format = remember { SimpleDateFormat("EEEE, MMM d • HH:mm", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                            semantic.cardElevated
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = format.format(date),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = stringResource(R.string.ride_details_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${String.format(Locale.getDefault(), "%.2f", ride.distanceKm)} ${stringResource(R.string.unit_km)} • ${formatDuration(ride.durationSec)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RideDetailMapCard(ride: RideSession) {
    val semantic = LocalSemanticColors.current
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(22.dp))
        ) {
            RideDetailMap(ride.pathPoints.map { LatLng(it.latitude, it.longitude) })
        }
    }
}

@Composable
private fun RideStatsCard(ride: RideSession) {
    val semantic = LocalSemanticColors.current
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.ride_stats_subtitle),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            HorizontalDivider(color = semantic.cardStroke)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RideStatItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Straighten,
                    value = String.format(Locale.getDefault(), "%.2f", ride.distanceKm),
                    unit = stringResource(R.string.unit_km),
                    label = stringResource(R.string.stat_distance)
                )
                RideStatItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.AccessTime,
                    value = formatDuration(ride.durationSec),
                    unit = "",
                    label = stringResource(R.string.stat_duration)
                )
                RideStatItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.LocalFireDepartment,
                    value = ride.calories.toString(),
                    unit = stringResource(R.string.unit_kcal),
                    label = stringResource(R.string.label_energy)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RideStatItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Speed,
                    value = String.format(Locale.getDefault(), "%.1f", ride.avgSpeedKmh),
                    unit = stringResource(R.string.unit_kmh),
                    label = stringResource(R.string.ride_avg_speed)
                )
                RideStatItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    value = String.format(Locale.getDefault(), "%.1f", ride.maxSpeedKmh),
                    unit = stringResource(R.string.unit_kmh),
                    label = stringResource(R.string.ride_max_speed)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RideStatItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    unit: String,
    label: String
) {
    val semantic = LocalSemanticColors.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardSubtle),
        border = BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
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
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoCard(
    text: String,
    containerBrush: Brush
) {
    val semantic = LocalSemanticColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerBrush)
                .padding(14.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun RideDetailMap(points: List<LatLng>) {
    val semantic = LocalSemanticColors.current
    val validPoints = points.filter { it.latitude != 0.0 || it.longitude != 0.0 }
    val displayedPoints = remember(validPoints) {
        if (validPoints.size > 500) {
            val step = validPoints.size / 500
            validPoints.filterIndexed { index, _ -> index % step == 0 || index == validPoints.lastIndex }
        } else {
            validPoints
        }
    }

    if (displayedPoints.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(semantic.cardSubtle),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_route_data),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(displayedPoints.first(), 15f)
    }

    LaunchedEffect(displayedPoints) {
        if (displayedPoints.size > 1) {
            val builder = LatLngBounds.Builder()
            displayedPoints.forEach { builder.include(it) }
            runCatching {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(builder.build(), 100)
                )
            }
        } else {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(displayedPoints.first(), 15f)
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(zoomControlsEnabled = false)
    ) {
        Polyline(
            points = displayedPoints,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
            width = 18f,
            geodesic = true,
            jointType = JointType.ROUND
        )
        Polyline(
            points = displayedPoints,
            color = MaterialTheme.colorScheme.primary,
            width = 9f,
            geodesic = true,
            jointType = JointType.ROUND
        )
        Marker(
            state = MarkerState(position = displayedPoints.first()),
            title = stringResource(R.string.map_start),
            icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN
            )
        )
        Marker(
            state = MarkerState(position = displayedPoints.last()),
            title = stringResource(R.string.map_end),
            icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
            )
        )
    }
}

private fun formatDuration(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }
}

@Composable
private fun rideTypeLabel(rideType: String?): String {
    return when (rideType?.lowercase(Locale.US)) {
        "dressage" -> stringResource(R.string.ride_type_dressage)
        "show_jumping" -> stringResource(R.string.ride_type_show_jumping)
        "endurance" -> stringResource(R.string.ride_type_endurance)
        "trail_riding" -> stringResource(R.string.ride_type_trail_riding)
        else -> rideType.orEmpty()
    }
}
