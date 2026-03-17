package com.horsegallop.feature.home.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.horsegallop.core.components.ActivityItem
import com.horsegallop.ui.theme.LocalSemanticColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentActivityDetailScreen(
    navController: NavController,
    onOpenRideDetail: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.ui.collectAsState()
    val semantic = LocalSemanticColors.current

    LaunchedEffect(Unit) {
        viewModel.loadRecentActivities(limit = 20)
    }

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(id = com.horsegallop.R.string.performance_board_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(com.horsegallop.R.string.back))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = semantic.screenTopBar
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.14f),
                            semantic.screenBase
                        )
                    )
                )
        ) {
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
                    SectionHeader(stringResource(id = com.horsegallop.R.string.weekly_progress))
                    Spacer(modifier = Modifier.height(12.dp))
                    AnimatedDistanceBarChart(dailyDistance = uiState.dailyDistance)
                }

                item {
                    SectionHeader(stringResource(id = com.horsegallop.R.string.activity_distribution_title))
                    Spacer(modifier = Modifier.height(12.dp))
                    AnimatedActivityPieChart(uiState.activityDistribution)
                }

                item {
                    SectionHeader(stringResource(id = com.horsegallop.R.string.history_title))
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(uiState.activities) { activity ->
                    ActivityItem(
                        title = activity.title ?: stringResource(id = com.horsegallop.R.string.ride_default_title),
                        subtitle = "${activity.dateLabel} • ${activity.timeLabel}",
                        duration = "${activity.durationMin} min",
                        distance = "${activity.distanceKm} km",
                        icon = Icons.AutoMirrored.Filled.DirectionsRun,
                        onClick = {
                            if (activity.id.isNotBlank()) {
                                onOpenRideDetail(activity.id)
                            }
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
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
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun AnimatedStatsSection(uiState: HomeUiState) {
    val semantic = LocalSemanticColors.current
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
                 value = uiState.totalDuration,
                 unit = "",
                 color = MaterialTheme.colorScheme.tertiary,
                 delayMillis = 200,
                 visible = visible
             )
             AnimatedMetricCard(
                 modifier = Modifier.weight(1f),
                 title = "Calories",
                 value = uiState.totalCalories,
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
        
        if (uiState.favoriteBarn != "Unknown") {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = semantic.cardSubtle
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
                            uiState.favoriteBarn, 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Bold
                        )
                    }
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
    val semantic = LocalSemanticColors.current
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
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
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
private fun AnimatedActivityPieChart(activityDistribution: List<Pair<String?, Float>>) {
    val semantic = LocalSemanticColors.current
    val data = if (activityDistribution.isNotEmpty()) {
        activityDistribution.map { (name, value) -> 
            Pair(name ?: "Unknown", value) 
        }
    } else {
        // Fallback or empty state could be better, but for now keeping some default or empty
        emptyList()
    }
    
    if (data.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(semantic.cardSubtle, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(id = com.horsegallop.R.string.no_activity_data), style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        semantic.info,
        semantic.warning
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
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
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
                        val color = colors[index % colors.size]
                        
                        drawArc(
                            color = color,
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
                }
                
                // Inner info
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val label = if (selectedIndex != -1) data[selectedIndex].first else stringResource(id = com.horsegallop.R.string.chart_total_label)
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            
            // Legend
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                data.take(3).forEachIndexed { index, pair ->
                    val isSelected = index == selectedIndex
                    val color = colors[index % colors.size]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { selectedIndex = if (selectedIndex == index) -1 else index }
                            .scale(if (isSelected) 1.1f else 1.0f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(color, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = pair.first,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1
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
private fun AnimatedDistanceBarChart(dailyDistance: List<Float>) {
    val semantic = LocalSemanticColors.current
    // Show last 7 days
    val chartData = dailyDistance.takeLast(7)
    
    if (chartData.all { it == 0f }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(semantic.cardSubtle, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(id = com.horsegallop.R.string.no_activity_data), style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val maxDistance = remember(chartData) { (chartData.maxOrNull() ?: 10f).toDouble() }
    val barColor = MaterialTheme.colorScheme.primary
    
    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationPlayed = true }
    
    val heightProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
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
                            color = semantic.mapGrid,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                    }

                    chartData.forEachIndexed { index, distance ->
                        val targetHeight = (distance / maxDistance * size.height).toFloat()
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
                            color = barColor,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, currentBarHeight),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                    }
                }
            }
            
            // X-Axis Labels (Day names)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                // Adjust to show last 7 days ending today
                val today = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) // Sun=1, Mon=2...
                // Map to 0-6 index where 0=Mon
                val todayIndex = if (today == 1) 6 else today - 2
                
                for (i in 0..6) {
                    val dayIndex = (todayIndex - (6 - i) + 7) % 7
                    Text(
                        text = days[dayIndex],
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecentActivityDetailScreenPreview() {
    MaterialTheme {
        AnimatedStatsSection(
            uiState = HomeUiState(
                loading = false,
                totalRides = "12",
                totalDistance = "87.4",
                totalDuration = "6h 30m",
                totalCalories = "3200",
                favoriteBarn = "Ankara Hipodromu",
                dailyDistance = listOf(5f, 0f, 8f, 3f, 12f, 0f, 6f)
            )
        )
    }
}
