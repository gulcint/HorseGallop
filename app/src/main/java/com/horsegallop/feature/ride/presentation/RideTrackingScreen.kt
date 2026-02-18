@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)
package com.horsegallop.feature.ride.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.materialIcons
import androidx.compose.ui.res.vectorResource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.horsegallop.core.R

@Composable
fun RideTrackingRoute(
    onHomeClick: () -> Unit,
    onBarnsClick: () -> Unit,
    viewModel: RideTrackingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    RideTrackingScreen(
        uiState = uiState,
        onToggleRide = viewModel::toggleRide,
        onSaveRide = { viewModel.saveCurrentRide(it) },
        onBack = onHomeClick,
        onViewBarns = onBarnsClick
    )
}

@Composable
fun RideTrackingScreen(
    uiState: com.horsegallop.feature.ride.presentation.RideUiState,
    onToggleRide: () -> Unit,
    onSaveRide: (String?) -> Unit,
    onBack: () -> Unit,
    onViewBarns: () -> Unit
) {
    val cameraPositionState = rememberCameraPositionState()
    
    // Auto-center map on user location updates
    LaunchedEffect(uiState.pathPoints) {
        if (uiState.pathPoints.isNotEmpty()) {
            val lastPoint = uiState.pathPoints.last()
            cameraPositionState.animate(
                update = com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(LatLng(lastPoint.latitude, lastPoint.longitude), 17f)
                )
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Modern Top Bar with Horse Theme
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "RIDE TRACKING",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.padding(8.dp).clickable(onClick = onBack)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                },
                actions = {
                    if (uiState.isRiding) {
                        // Save Ride Button
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.padding(8.dp).clickable {
                                onSaveRide(uiState.selectedBarn?.barn?.name)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Save,
                                contentDescription = "Save Ride",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                    
                    // Barn Selector
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        modifier = Modifier.padding(8.dp).clickable(onClick = onViewBarns)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Select Barn",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Full Screen Map with Path
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false
                ),
                properties = MapProperties(
                    isMyLocationEnabled = true
                )
            ) {
                // Draw Path with Horse Theme Colors
                if (uiState.pathPoints.size > 1) {
                    val points = uiState.pathPoints.map { LatLng(it.latitude, it.longitude) }
                    
                    // Gradient path
                    val gradientColors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                    
                    Polyline(
                        points = points,
                        color = Brush.sweepGradient(
                            center = androidx.compose.ui.geometry.Center,
                            colors = gradientColors
                        ),
                        width = 14f,
                        geodesic = true
                    )
                    
                    // Start marker
                    val start = uiState.pathPoints.first()
                    Marker(
                        state = MarkerState(LatLng(start.latitude, start.longitude)),
                        title = "Start",
                        snippet = "Start of ride"
                    )
                    
                    // End marker
                    val end = uiState.pathPoints.last()
                    Marker(
                        state = MarkerState(LatLng(end.latitude, end.longitude)),
                        title = "End",
                        snippet = "End of ride"
                    )
                    
                    // Current position marker if riding
                    if (uiState.isRiding && uiState.pathPoints.isNotEmpty()) {
                        val current = uiState.pathPoints.last()
                        Marker(
                            state = MarkerState(LatLng(current.latitude, current.longitude)),
                            icon = BitmapDescriptorFactory.defaultMarker()
                        )
                    }
                }
            }
            
            // Floating Speedometer (Top Center) - Modern Glassmorphism
            if (uiState.isRiding) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = paddingValues.calculateTopPadding() + 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xE61C1C1E), // Dark with transparency
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = formatSpeed(uiState.speedKmh),
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "CURRENT SPEED",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            
            // Bottom Control Panel - Modern Equine Style with Glassmorphism
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // Dashboard Card - Modern with Gradient Overlay
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    elevation = CardDefaults.cardElevation(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Timer Display (Hero Section)
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = formatDuration(uiState.durationSec),
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontSize = 60.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "DURATION",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Metrics Grid - Modern 3-column layout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Distance Card - Modern with gradient card
                            MetricCard(
                                value = "%.1f".format(uiState.distanceKm),
                                unit = "km",
                                label = "DISTANCE",
                                icon = Icons.Filled.Directions,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            // Speed Card - Using Car/Transport icon for speed
                            MetricCard(
                                value = formatSpeed(uiState.speedKmh),
                                unit = "km/h",
                                label = "AVG SPEED",
                                icon = androidx.compose.material.icons.filled.AirplanemodeActive,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            
                            // Calories Card - Using Fire/Health icon
                            MetricCard(
                                value = "${uiState.calories}",
                                unit = "kcal",
                                label = "CALORIES",
                                icon = androidx.compose.material.icons.filled.FitnessCenter,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Active Ride Status Indicator with Glassmorphism
                        if (uiState.isRiding) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.error
                                        ) {
                                            Box(
                                                modifier = Modifier.size(12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Box(
                                                    modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.error)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "LIVE TRACKING",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Main Action Button
                        val playIcon = if (uiState.isRiding) Icons.Filled.Stop else Icons.Filled.PlayCircle
                        Button(
                            onClick = onToggleRide,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                            shape = RoundedCornerShape(36.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isRiding) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = playIcon,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = if (uiState.isRiding) "STOP RIDE" else "START RIDE",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Secondary Action - View Route Map
                        if (uiState.pathPoints.size > 1) {
                            TextButton(
                                onClick = {}, // Open full route map
                                modifier = Modifier.padding(top = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Map,
                                    contentDescription = "View Route",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "VIEW COMPLETE ROUTE",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    value: String,
    unit: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun formatDuration(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        "%02d:%02d:%02d".format(h, m, s)
    } else {
        "%02d:%02d".format(m, s)
    }
}

@Composable
fun formatSpeed(speed: Float): String {
    return if (speed >= 0) {
        "%.1f".format(speed)
    } else {
        "0.0"
    }
}
