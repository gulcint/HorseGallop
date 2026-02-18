@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.horsegallop.feature.ride.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.horsegallop.core.components.HorseGallopButton
import com.horsegallop.domain.ride.model.GeoPoint
import com.horsegallop.core.R as CoreR

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
        onBack = onHomeClick
    )
}

@Composable
fun RideTrackingScreen(
    uiState: RideUiState,
    onToggleRide: () -> Unit,
    onBack: () -> Unit
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
            // Transparent Top Bar
            CenterAlignedTopAppBar(
                title = { },
                navigationIcon = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(CoreR.string.back)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Full Screen Map
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
                // Draw Path
                if (uiState.pathPoints.size > 1) {
                    Polyline(
                        points = uiState.pathPoints.map { LatLng(it.latitude, it.longitude) },
                        color = MaterialTheme.colorScheme.primary,
                        width = 12f,
                        geodesic = true
                    )
                    val start = uiState.pathPoints.first()
                    val end = uiState.pathPoints.last()
                    Marker(state = MarkerState(LatLng(start.latitude, start.longitude)))
                    Marker(state = MarkerState(LatLng(end.latitude, end.longitude)))
                }
            }
            
            // Bottom Control Panel (CarPlay Style)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Dashboard Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1C1C1E).copy(alpha = 0.95f) // Apple dark gray
                    ),
                    elevation = CardDefaults.cardElevation(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Main Timer (Hero)
                        Text(
                            text = formatDuration(uiState.durationSec),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 64.sp,
                                letterSpacing = (-2).sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "DURATION",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Grid Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DashboardStat(
                                value = "%.1f".format(uiState.distanceKm),
                                unit = "km",
                                label = "DISTANCE",
                                modifier = Modifier.weight(1f)
                            )
                            // Divider
                            Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.DarkGray))
                            
                            DashboardStat(
                                value = "%.1f".format(uiState.speedKmh),
                                unit = "km/h",
                                label = "SPEED",
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Divider
                            Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.DarkGray))

                            DashboardStat(
                                value = "${uiState.calories}",
                                unit = "kcal",
                                label = "CALORIES",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Action Button
                        Button(
                            onClick = onToggleRide,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(30.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isRiding) Color(0xFFFF3B30) else Color(0xFF34C759) // Apple Red/Green
                            )
                        ) {
                            Icon(
                                imageVector = if (uiState.isRiding) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (uiState.isRiding) "STOP RIDE" else "START RIDE",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardStat(
    value: String,
    unit: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDuration(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        "%02d:%02d:%02d".format(h, m, s)
    } else {
        "%02d:%02d".format(m, s)
    }
}
