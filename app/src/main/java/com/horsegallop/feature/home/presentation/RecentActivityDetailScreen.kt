package com.horsegallop.feature.home.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.horsegallop.compose.ActivityItem
import com.horsegallop.compose.MetricCard
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentActivityDetailScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.ui.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRecentActivities(limit = 20)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Performance Board",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 16.dp)
        ) {
            item {
                AnimatedStatsSection(uiState)
            }

            item {
                SectionHeader("Weekly Progress")
                Spacer(modifier = Modifier.height(12.dp))
                AnimatedDistanceBarChart(activities = uiState.activities)
            }
            
            item {
                SectionHeader("Activity Distribution")
                Spacer(modifier = Modifier.height(12.dp))
                AnimatedActivityPieChart()
            }

            item {
                SectionHeader("History")
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(uiState.activities) { activity ->
                ActivityItem(
                    title = activity.title,
                    subtitle = "${activity.dateLabel} • ${activity.timeLabel}",
                    duration = "${activity.durationMin} min",
                    distance = "${activity.distanceKm} km",
                    icon = Icons.AutoMirrored.Filled.DirectionsRun
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
    )
}

@Composable
private fun AnimatedStatsSection(uiState: HomeUiState) {
    val totalDurationMin = uiState.activities.sumOf { it.durationMin }
    val totalCalories = totalDurationMin * 6 // Avg 6 kcal/min for riding
    val mostVisited = "Sunrise Stables" // Mock

    // Staggered animation state
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Top Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
             AnimatedMetricCard(
                 modifier = Modifier.weight(1f),
                 title = "Total Rides",
                 value = uiState.totalRides,
                 unit = "",
                 color = MaterialTheme.colorScheme.primary,
                 delayMillis = 0,
                 visible = visible
             )
             AnimatedMetricCard(
                 modifier = Modifier.weight(1f),
                 title = "Distance",
                 value = uiState.totalDistance,
                 unit = "km",
                 color = MaterialTheme.colorScheme.secondary,
                 delayMillis = 100,
                 visible = visible
             )
        }
        
        // Second Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
             AnimatedMetricCard(
                 modifier = Modifier.weight(1f),
                 title = "Total Time",
                 value = "${totalDurationMin / 60}h ${totalDurationMin % 60}m",
                 unit = "",
                 color = MaterialTheme.colorScheme.tertiary,
                 delayMillis = 200,
                 visible = visible
             )
             AnimatedMetricCard(
                 modifier = Modifier.weight(1f),
                 title = "Calories",
                 value = "$totalCalories",
                 unit = "kcal",
                 color = MaterialTheme.colorScheme.error,
                 delayMillis = 300,
                 visible = visible
             )
        }

        // Most Visited Barn Card
        val scale by animateFloatAsState(
            targetValue = if (visible) 1f else 0.8f,
            animationSpec = tween(500, delayMillis = 400)
        )
        val alpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(500, delayMillis = 400)
        )
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .alpha(alpha)
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Star, 
                        null, 
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "Favorite Stable", 
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        mostVisited, 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    color: Color,
    delayMillis: Int,
    visible: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = tween(500, delayMillis = delayMillis)
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500, delayMillis = delayMillis)
    )

    Card(
        modifier = modifier
            .scale(scale)
            .alpha(alpha),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedActivityPieChart() {
    val data = listOf(
        Pair("Trail", 0.4f),
        Pair("Lesson", 0.35f),
        Pair("Training", 0.25f)
    )
    val colors = listOf(
        Color(0xFF5D4037), // Saddle Brown dark
        Color(0xFF8B4513), // Saddle Brown
        Color(0xFFD2691E)  // Chocolate
    )
    
    var selectedIndex by remember { mutableStateOf(-1) }
    var animationPlayed by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .height(200.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Chart
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { selectedIndex = -1 }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = -90f
                    val totalSweep = 360f * animProgress
                    
                    data.forEachIndexed { index, pair ->
                        val sweepAngle = pair.second * totalSweep
                        val isSelected = index == selectedIndex
                        val scale = if (isSelected) 1.1f else 1.0f
                        
                        drawArc(
                            color = colors[index],
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 40f * scale, cap = StrokeCap.Butt),
                            size = Size(size.width * scale, size.height * scale),
                            topLeft = Offset(
                                (size.width - size.width * scale) / 2,
                                (size.height - size.height * scale) / 2
                            )
                        )
                        startAngle += sweepAngle
                    }
                    
                    // Center Text
                    if (selectedIndex != -1) {
                        // Draw text logic if needed, omitted for simplicity
                    }
                }
                
                // Inner info
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val label = if (selectedIndex != -1) data[selectedIndex].first else "Total"
                    val value = if (selectedIndex != -1) "${(data[selectedIndex].second * 100).toInt()}%" else "100%"
                    
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Legend
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                data.forEachIndexed { index, pair ->
                    val isSelected = index == selectedIndex
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { selectedIndex = if (selectedIndex == index) -1 else index }
                            .scale(if (isSelected) 1.1f else 1.0f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(colors[index], CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = pair.first,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = "${(pair.second * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedDistanceBarChart(activities: List<ActivityUi>) {
    // Show last 7 activities
    val chartData = remember(activities) { activities.take(7).reversed() }
    
    if (chartData.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("No activity data available", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val maxDistance = remember(chartData) { chartData.maxOfOrNull { it.distanceKm } ?: 10.0 }
    val barColor = MaterialTheme.colorScheme.primary
    
    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationPlayed = true }
    
    val heightProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barWidth = size.width / (chartData.size * 2f + 1)
                    val space = size.width / chartData.size
                    
                    // Draw grid lines
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = size.height * (i.toFloat() / gridLines)
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                    }

                    chartData.forEachIndexed { index, activity ->
                        val targetHeight = (activity.distanceKm / maxDistance * size.height).toFloat()
                        val currentBarHeight = targetHeight * heightProgress
                        val x = index * space + (space - barWidth) / 2
                        val y = size.height - currentBarHeight
                        
                        // Bar shadow
                        drawRoundRect(
                            color = barColor.copy(alpha = 0.2f),
                            topLeft = Offset(x + 4f, y + 4f),
                            size = Size(barWidth, currentBarHeight),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                        
                        // Actual bar
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(barColor, barColor.copy(alpha = 0.7f))
                            ),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, currentBarHeight),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                chartData.forEach { activity ->
                    Text(
                        text = activity.dateLabel.take(3),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(30.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

