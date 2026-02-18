@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)
package com.horsegallop.feature.ride.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.core.R
import com.horsegallop.core.components.BottomNavigationItem
import com.horsegallop.core.components.HorseGallopBottomNavigation
import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.core.theme.*

/**
 * Modern Ride Start Screen - Horse Theme with Improved UX/UI
 */
@Composable
fun RideStartRoute(
    onHomeClick: () -> Unit,
    onBarnsClick: () -> Unit,
    viewModel: RideStartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    RideStartScreen(
        uiState = uiState,
        onStartRide = { viewModel.startRide() },
        onStopRide = { viewModel.stopRide() },
        onSaveRide = { viewModel.saveCurrentRide(it) },
        onBack = onHomeClick,
        onViewBarns = onBarnsClick
    )
}

@Composable
fun RideStartScreen(
    uiState: RideUiState,
    onStartRide: () -> Unit,
    onStopRide: () -> Unit,
    onSaveRide: (String?) -> Unit,
    onBack: () -> Unit,
    onViewBarns: () -> Unit
) {
    // Animated values for modern design
    val cardElevation = animateDpAsState(targetValue = if (uiState.isRiding) 12.dp else 4.dp).value
    val containerColor = animateDpAsState(targetValue = if (uiState.isRiding) 8.dp else 4.dp).value
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(id = R.string.app_name),
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
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Header Section
            Text(
                text = stringResource(id = R.string.qa_start_ride_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Main Action Button - Large & Iconic
            val playIcon = if (uiState.isRiding) Icons.Filled.Stop else Icons.Filled.PlayCircle
            val buttonColor = if (uiState.isRiding) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            
            Surface(
                shape = CircleShape,
                color = buttonColor,
                modifier = Modifier
                    .size(120.dp)
                    .clickable(onClick = if (uiState.isRiding) onStopRide else onStartRide),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = playIcon,
                        contentDescription = if (uiState.isRiding) "Stop Ride" else "Start Ride",
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (uiState.isRiding) stringResource(id = R.string.stop_ride_label) else stringResource(id = R.string.start_ride_label),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (uiState.isRiding) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Live Metrics Dashboard
            Text(
                text = stringResource(id = R.string.live_metrics_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Distance Card
                MetricCard(
                    value = "%.1f".format(uiState.distanceKm),
                    unit = "km",
                    label = stringResource(id = R.string.distance_label),
                    icon = Icons.Filled.Directions,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Speed Card
                MetricCard(
                    value = formatSpeed(uiState.speedKmh),
                    unit = "km/h",
                    label = stringResource(id = R.string.speed_label),
                    icon = Icons.Filled.Speed,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                // Calories Card
                MetricCard(
                    value = "${uiState.calories}",
                    unit = "kcal",
                    label = stringResource(id = R.string.calories_label),
                    icon = Icons.Filled.LocalFireDepartment,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Timer Display Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = formatDuration(uiState.durationSec),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 72.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = stringResource(id = R.string.duration_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (uiState.isRiding) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            modifier = Modifier.padding(horizontal = 32.dp)
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
                                        text = stringResource(id = R.string.live_tracking_active),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = stringResource(id = R.string.tap_start_to_begin),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Barn Selection Card
            if (uiState.barns.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(id = R.string.select_barn_label),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* Open barn selector */ }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = uiState.selectedBarn?.barn?.name ?: stringResource(id = R.string.tap_to_select_barn),
                                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Save Ride Button (if riding)
            if (uiState.isRiding) {
                Surface(
                    shape = RoundedCornerShape(36.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable { onSaveRide(uiState.selectedBarn?.barn?.name) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.save_and_end_ride),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Modern Metric Card Design
 */
@Composable
fun MetricCard(
    value: String,
    unit: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Modern Gradient Background - Horse Theme
 */
@Composable
fun getGradientBackground(): Brush {
    return Brush.linearGradient(
        colors = listOf(
            Background,
            SurfaceVariant
        ),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
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
    return if (speed >= 0f) {
        "%.1f".format(speed)
    } else {
        "0.0"
    }
}
