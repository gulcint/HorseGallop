package com.horsegallop.feature.ride.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.text.SimpleDateFormat
import java.util.*

import com.horsegallop.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideDetailScreen(
    onBack: () -> Unit,
    viewModel: RideDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val ride = uiState.ride

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ride_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (ride != null) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Map Section
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    ) {
                        RideDetailMap(ride.pathPoints.map { LatLng(it.latitude, it.longitude) })
                    }

                    // Stats Section
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Date Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val date = Date(ride.dateMillis)
                            val format = SimpleDateFormat("EEEE, MMM d • HH:mm", Locale.getDefault())
                            Text(
                                text = format.format(date),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Metrics Grid
                        Row(modifier = Modifier.fillMaxWidth()) {
                            StatItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Speed,
                                value = String.format("%.2f", ride.distanceKm),
                                unit = stringResource(R.string.unit_km),
                                label = stringResource(R.string.stat_distance)
                            )
                            StatItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.AccessTime,
                                value = formatDuration(ride.durationSec),
                                unit = stringResource(R.string.unit_time),
                                label = stringResource(R.string.stat_duration)
                            )
                            StatItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.LocalFireDepartment,
                                value = "${ride.calories}",
                                unit = stringResource(R.string.unit_kcal),
                                label = stringResource(R.string.label_energy)
                            )
                        }
                        
                        if (!ride.barnName.isNullOrEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(R.string.riding_at, ride.barnName ?: ""),
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            } else if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.ride_not_found))
                }
            }
        }
    }
}

@Composable
fun RideDetailMap(points: List<LatLng>) {
    // Filter invalid 0,0 points
    val validPoints = points.filter { it.latitude != 0.0 || it.longitude != 0.0 }
    
    // Sample points if too many (to prevent freezing)
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
                .background(Color(0xFFE0E0E0)), // Placeholder gray
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.no_route_data), color = Color.Gray)
        }
        return
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(displayedPoints.first(), 15f)
    }
    
    // Auto-fit bounds
    LaunchedEffect(displayedPoints) {
        if (displayedPoints.size > 1) {
            val builder = com.google.android.gms.maps.model.LatLngBounds.Builder()
            displayedPoints.forEach { builder.include(it) }
            try {
                cameraPositionState.animate(
                    com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(builder.build(), 100)
                )
            } catch (e: Exception) {
                // Ignore layout errors
            }
        } else if (displayedPoints.isNotEmpty()) {
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
            color = MaterialTheme.colorScheme.primary,
            width = 12f,
            geodesic = true
        )
        Marker(
            state = MarkerState(position = displayedPoints.first()),
            title = stringResource(R.string.map_start),
            icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN)
        )
        Marker(
            state = MarkerState(position = displayedPoints.last()),
            title = stringResource(R.string.map_end),
            icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED)
        )
    }
}

@Composable
fun StatItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    unit: String,
    label: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon, 
            contentDescription = null, 
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDuration(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
}
