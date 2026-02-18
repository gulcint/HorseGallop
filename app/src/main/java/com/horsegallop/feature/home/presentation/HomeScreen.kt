@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.horsegallop.feature.home.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberLazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.horsegallop.core.R
import com.horsegallop.core.components.*
import com.horsegallop.core.theme.*
import com.horsegallop.navigation.Dest
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.lazy.items

@Composable
fun HomeScreen(
    onStartRide: () -> Unit,
    onViewBarns: () -> Unit,
    onProfileClick: () -> Unit,
    onViewAllActivities: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.ui.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = stringResource(id = R.string.app_name),
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onProfileClick) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(WarmClay.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = WarmClay
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    navigationIconContentColor = WarmClay,
                    titleContentColor = Primary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            userScrollEnabled = true
        ) {
            // Welcome Card
            item {
                WelcomeCard(
                    userName = "Rider",
                    onProfileClick = onProfileClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Quick Actions
            item {
                QuickActionsSection(
                    onStartRide = onStartRide,
                    onViewBarns = onViewBarns,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Stats Overview
            item {
                StatsOverviewSection(
                    totalRides = uiState.totalRides,
                    totalDistance = uiState.totalDistance,
                    totalDuration = uiState.totalDuration,
                    totalCalories = uiState.totalCalories,
                    favoriteBarn = uiState.favoriteBarn,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Recent Activities
            item {
                RecentActivitySection(
                    activities = uiState.activities,
                    onViewAllActivities = onViewAllActivities,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Riding Tip
            item {
                TipsSection(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun WelcomeCard(
    userName: String,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Text(
                    text = "Welcome back, $userName!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onProfileClick() }
            ) {
                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = WarmClay.copy(alpha = 0.1f)),
                    border = BorderStroke(2.dp, WarmClay.copy(alpha = 0.3f))
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = WarmClay
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    onStartRide: () -> Unit,
    onViewBarns: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.quick_actions_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = stringResource(id = R.string.qa_start_ride_title),
                    subtitle = stringResource(id = R.string.qa_start_ride_subtitle),
                    icon = Icons.Filled.PlayArrow,
                    color = Primary,
                    onClick = onStartRide,
                    modifier = Modifier.weight(1f)
                )
                
                QuickActionCard(
                    title = stringResource(id = R.string.qa_view_barns_title),
                    subtitle = stringResource(id = R.string.qa_view_barns_subtitle),
                    icon = Icons.Filled.LocationOn,
                    color = WarmClay,
                    onClick = onViewBarns,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatsOverviewSection(
    totalRides: String,
    totalDistance: String,
    totalDuration: String,
    totalCalories: String,
    favoriteBarn: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.stats_yours_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = stringResource(id = R.string.stats_total_rides),
                    value = totalRides,
                    subtitle = stringResource(id = R.string.stats_rides_suffix),
                    icon = Icons.Filled.Timer,
                    color = Primary,
                    modifier = Modifier.weight(1f)
                )
                
                StatCard(
                    title = stringResource(id = R.string.stats_distance),
                    value = totalDistance,
                    subtitle = stringResource(id = R.string.stats_km_suffix),
                    icon = Icons.Filled.Speed,
                    color = WarmClay,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = stringResource(id = R.string.stats_calories),
                    value = totalCalories,
                    subtitle = stringResource(id = R.string.stats_calories_suffix),
                    icon = Icons.Filled.Firebase,
                    color = Primary,
                    modifier = Modifier.weight(1f)
                )
                
                StatCard(
                    title = stringResource(id = R.string.stats_duration),
                    value = totalDuration,
                    subtitle = stringResource(id = R.string.stats_hours_suffix),
                    icon = Icons.Filled.Schedule,
                    color = WarmClay,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun RecentActivitySection(
    activities: List<ActivityUi>,
    onViewAllActivities: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.recent_activity_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                
                if (onViewAllActivities != null) {
                    Text(
                        text = stringResource(id = R.string.view_all),
                        color = Primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onViewAllActivities() }
                    )
                }
            }

            if (activities.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.History,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(id = R.string.no_activities),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    activities.take(2).forEach { activity ->
                        ActivityItem(
                            title = activity.title ?: stringResource(id = R.string.ride_default_title),
                            subtitle = "${activity.dateLabel} • ${activity.timeLabel}",
                            duration = stringResource(id = R.string.activity_duration_minutes, activity.durationMin),
                            distance = stringResource(id = R.string.activity_distance_km, activity.distanceKm),
                            icon = Icons.AutoMirrored.Filled.DirectionsRun,
                            color = Primary
                        )
                        
                        if (activities.indexOf(activity) < 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TipsSection(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = WarmClay.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Filled.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = WarmClay
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = stringResource(id = R.string.riding_tips_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Text(
                    text = stringResource(id = R.string.tip_safe_riding_title),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun PreviewHomeScreen() {
    MaterialTheme {
        HomeScreen(
            onStartRide = {},
            onViewBarns = {},
            onProfileClick = {}
        )
    }
}
