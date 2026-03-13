package com.horsegallop.feature.ride.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.horsegallop.domain.ride.model.GeoPoint
import com.horsegallop.domain.ride.model.RideSession
import com.horsegallop.domain.ride.util.gaitOf
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
                    if (ride.pathPoints.any { it.altitudeM != 0f }) {
                        item {
                            ElevationProfileCard(
                                pathPoints = ride.pathPoints,
                                totalDistanceKm = ride.distanceKm
                            )
                        }
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
            RideDetailMap(ride.pathPoints)
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

            // Gait distribution — only shown for rides with speed data
            if (ride.pathPoints.isNotEmpty() && ride.pathPoints.any { it.speedKmh > 0f }) {
                val total = ride.pathPoints.size.toFloat()
                val walkPct = (ride.pathPoints.count { gaitOf(it.speedKmh) == "walk" } / total * 100).toInt()
                val trotPct = (ride.pathPoints.count { gaitOf(it.speedKmh) == "trot" } / total * 100).toInt()
                val canterPct = (ride.pathPoints.count { gaitOf(it.speedKmh) == "canter" } / total * 100).toInt()
                val semantic = LocalSemanticColors.current
                HorizontalDivider(color = semantic.cardStroke)
                Text(
                    text = stringResource(R.string.gait_distribution),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GaitBar(modifier = Modifier.weight(1f), color = semantic.gaitWalk, label = stringResource(R.string.gait_walk), pct = walkPct)
                    GaitBar(modifier = Modifier.weight(1f), color = semantic.gaitTrot, label = stringResource(R.string.gait_trot), pct = trotPct)
                    GaitBar(modifier = Modifier.weight(1f), color = semantic.gaitCanter, label = stringResource(R.string.gait_canter), pct = canterPct)
                }
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
fun RideDetailMap(geoPoints: List<GeoPoint>) {
    val semantic = LocalSemanticColors.current
    val validGeo = geoPoints.filter { it.latitude != 0.0 || it.longitude != 0.0 }
    val displayedGeo = remember(validGeo) {
        if (validGeo.size > 500) {
            val step = validGeo.size / 500
            validGeo.filterIndexed { i, _ -> i % step == 0 || i == validGeo.lastIndex }
        } else {
            validGeo
        }
    }

    if (displayedGeo.isEmpty()) {
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
        position = CameraPosition.fromLatLngZoom(
            LatLng(displayedGeo.first().latitude, displayedGeo.first().longitude), 15f
        )
    }

    LaunchedEffect(displayedGeo) {
        if (displayedGeo.size > 1) {
            val builder = LatLngBounds.Builder()
            displayedGeo.forEach { builder.include(LatLng(it.latitude, it.longitude)) }
            runCatching { cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(builder.build(), 100)) }
        } else {
            val pt = displayedGeo.first()
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(pt.latitude, pt.longitude), 15f)
        }
    }

    val gaitSegments = remember(displayedGeo) { buildGaitSegments(displayedGeo) }
    val hasGaitData = displayedGeo.any { it.speedKmh > 0f }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            if (hasGaitData) {
                gaitSegments.forEach { (segPoints, gait) ->
                    val segColor = when (gait) {
                        "trot"   -> semantic.gaitTrot
                        "canter" -> semantic.gaitCanter
                        else     -> semantic.gaitWalk
                    }
                    if (segPoints.size >= 2) {
                        Polyline(points = segPoints, color = segColor.copy(alpha = 0.25f), width = 18f, geodesic = true, jointType = JointType.ROUND)
                        Polyline(points = segPoints, color = segColor, width = 9f, geodesic = true, jointType = JointType.ROUND)
                    }
                }
            } else {
                // Fallback: solid primary color for older rides without speed data
                val allPoints = displayedGeo.map { LatLng(it.latitude, it.longitude) }
                Polyline(points = allPoints, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), width = 18f, geodesic = true, jointType = JointType.ROUND)
                Polyline(points = allPoints, color = MaterialTheme.colorScheme.primary, width = 9f, geodesic = true, jointType = JointType.ROUND)
            }
            Marker(
                state = MarkerState(position = LatLng(displayedGeo.first().latitude, displayedGeo.first().longitude)),
                title = stringResource(R.string.map_start),
                icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                    com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN
                )
            )
            Marker(
                state = MarkerState(position = LatLng(displayedGeo.last().latitude, displayedGeo.last().longitude)),
                title = stringResource(R.string.map_end),
                icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                    com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
                )
            )
        }
        // Gait legend overlay — only when speed data is available
        if (hasGaitData) {
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
                DetailGaitDot(color = semantic.gaitWalk, label = stringResource(R.string.gait_walk))
                DetailGaitDot(color = semantic.gaitTrot, label = stringResource(R.string.gait_trot))
                DetailGaitDot(color = semantic.gaitCanter, label = stringResource(R.string.gait_canter))
            }
        }
    }
}

@Composable
private fun GaitBar(
    modifier: Modifier = Modifier,
    color: Color,
    label: String,
    pct: Int
) {
    val semantic = LocalSemanticColors.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$pct%",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(semantic.cardSubtle)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun DetailGaitDot(color: Color, label: String) {
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

@Composable
private fun ElevationProfileCard(pathPoints: List<GeoPoint>, totalDistanceKm: Float = 0f) {
    val semantic = LocalSemanticColors.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline

    val altitudes = remember(pathPoints) { pathPoints.map { it.altitudeM } }
    val minAlt = remember(altitudes) { altitudes.minOrNull() ?: 0f }
    val maxAlt = remember(altitudes) { altitudes.maxOrNull() ?: 0f }
    val altRange = (maxAlt - minAlt).coerceAtLeast(1f)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.elevation_profile_title),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "↑ ${maxAlt.toInt()} m  ↓ ${minAlt.toInt()} m",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor
                )
            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            ) {
                val w = size.width
                val h = size.height
                val n = (pathPoints.size - 1).coerceAtLeast(1)
                val linePath = androidx.compose.ui.graphics.Path()
                pathPoints.forEachIndexed { i, point ->
                    val x = w * i / n
                    val y = h - h * (point.altitudeM - minAlt) / altRange
                    if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
                }
                val fillPath = androidx.compose.ui.graphics.Path().apply {
                    addPath(linePath)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    fillPath,
                    brush = Brush.verticalGradient(
                        listOf(primaryColor.copy(alpha = 0.22f), primaryColor.copy(alpha = 0.03f)),
                        startY = 0f, endY = h
                    )
                )
                drawPath(
                    linePath,
                    color = primaryColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0 km", style = MaterialTheme.typography.labelSmall, color = outlineColor)
                Text(
                    text = String.format(Locale.getDefault(), "%.1f km", totalDistanceKm),
                    style = MaterialTheme.typography.labelSmall,
                    color = outlineColor
                )
            }
        }
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
