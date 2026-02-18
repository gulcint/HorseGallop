package com.horsegallop.feature.ride.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.text.SimpleDateFormat
import java.util.*

import com.horsegallop.core.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideDetailScreen(
    onBack: () -> Unit,
    viewModel: RideDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val ride = uiState.ride

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (ride != null) {
                // 1. Full Screen Map Background (Top Half)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp) // Taller map
                ) {
                    RideDetailMap(ride.pathPoints.map { LatLng(it.latitude, it.longitude) })
                    
                    // Gradient overlay for seamless transition
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                                        MaterialTheme.colorScheme.surface
                                    ),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY
                                )
                            )
                    )
                }

                // 2. Back Button (Floating)
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(top = 48.dp, start = 16.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 3. Bottom Sheet / Details Panel
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 320.dp) // Start overlapping the map
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp,
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Drag Handle (Visual cue)
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))

                            // Date & Time
                            val date = Date(ride.dateMillis)
                            val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
                            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                            
                            // Dynamic Title Logic
                            val calendar = Calendar.getInstance().apply { time = date }
                            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
                            val rideTitle = when (hourOfDay) {
                                in 5..11 -> "Morning Ride"
                                in 12..16 -> "Afternoon Ride"
                                in 17..20 -> "Evening Ride"
                                else -> "Night Ride"
                            }
                            
                            Text(
                                text = dateFormat.format(date).uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = rideTitle,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Hero Metric: Distance
                            Text(
                                text = String.format("%.2f", ride.distanceKm),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 64.sp,
                                    letterSpacing = (-2).sp
                                ),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.unit_km).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(40.dp))

                            // Stats Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DetailStatItem(
                                    value = formatDuration(ride.durationSec),
                                    label = "DURATION",
                                    icon = Icons.Default.AccessTime,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant))

                                DetailStatItem(
                                    value = String.format("%.1f", ride.distanceKm / (ride.durationSec / 3600f).coerceAtLeast(0.001f)), // Calc avg speed roughly
                                    label = "AVG SPEED",
                                    unit = "km/h",
                                    icon = Icons.Default.Speed,
                                    modifier = Modifier.weight(1f)
                                )

                                Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant))

                                DetailStatItem(
                                    value = "${ride.calories}",
                                    label = "CALORIES",
                                    unit = "kcal",
                                    icon = Icons.Default.LocalFireDepartment,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            // Barn Chip
                            if (!ride.barnName.isNullOrEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.LocationOn, // Assuming LocationOn is available or use another
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = ride.barnName ?: "",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
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
fun DetailStatItem(
    value: String,
    label: String,
    unit: String? = null,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (unit != null) {
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
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

private fun formatDuration(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
}
