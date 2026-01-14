package com.horsegallop.feature.barn.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.horsegallop.navigation.Dest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.FilterList
import com.horsegallop.core.R
import com.horsegallop.domain.barn.model.BarnUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarnsMapViewScreen(
    navController: NavController,
    viewModel: BarnViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val barns = uiState.filteredBarns
    
    // Zoom state
    var zoomLevel by remember { mutableFloatStateOf(1f) }
    val maxZoom = 3f
    val minZoom = 0.5f
    
    // Grouping
    val groupedBarns = remember(barns, zoomLevel) {
        groupBarnsByProximity(barns.map { it.barn }, zoomLevel)
    }

    // Filters state
    val filters = listOf("Open Now", "Lessons", "Boarding", "Competition", "Clinics")
    val selectedFilters = remember { mutableStateListOf<String>() }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F1ED)) // Map-like background (Light Beige)
    ) {
        // Map Layer
        BarnsMapCanvas(
            groups = groupedBarns,
            allBarnsBounds = barns.map { it.barn },
            zoomLevel = zoomLevel,
            onGroupClick = { group ->
                if (group.barns.size == 1) {
                    navController.navigate("barnDetail/${group.barns.first().id}")
                } else {
                    zoomLevel = (zoomLevel * 1.5f).coerceAtMost(maxZoom)
                }
            }
        )

        // Top Controls Layer (SafeArea)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            // Original-Style Modern Search Bar & Back Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Circular Back Button
                Surface(
                    onClick = { navController.navigateUp() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Unified Search Field part (now matches map style exactly)
                com.horsegallop.core.components.HorseGallopSearchBar(
                    query = "", 
                    onQueryChange = { /* Handle map search */ },
                    placeholder = "Search area...",
                    modifier = Modifier.weight(1f)
                )
            }


            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilters.contains(filter)
                    FilterChip(
                        selected = isSelected,
                        onClick = { 
                            if (isSelected) selectedFilters.remove(filter) 
                            else selectedFilters.add(filter)
                        },
                        label = { Text(filter) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = Color.Transparent,
                            enabled = true,
                            selected = isSelected
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = FilterChipDefaults.filterChipElevation(elevation = 2.dp)
                    )
                }
            }
        }
        
        // Right Side Controls (Zoom & My Location)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // My Location Button
            FloatingActionButton(
                onClick = { /* Center on user */ },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Filled.LocationOn, "My Location")
            }

            // Zoom Controls
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                modifier = Modifier.width(48.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = { zoomLevel = (zoomLevel * 1.2f).coerceAtMost(maxZoom) }) {
                        Icon(Icons.Filled.Add, "Zoom In")
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    IconButton(onClick = { zoomLevel = (zoomLevel / 1.2f).coerceAtLeast(minZoom) }) {
                        Icon(Icons.Filled.Remove, "Zoom Out")
                    }
                }
            }
        }

        // Bottom Carousel (Barns)
        if (barns.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
            ) {
                // "Search this area" button (Visible when moved - simulated always visible for now)
                Button(
                    onClick = { /* Re-search */ },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                ) {
                    Text("Search this area", fontWeight = FontWeight.SemiBold)
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(barns.take(10)) { barnWithLocation ->
                        BarnListItemHorizontal(
                            barn = barnWithLocation.barn,
                            onClick = { navController.navigate("barnDetail/${barnWithLocation.barn.id}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BarnsMapCanvas(
    groups: List<BarnGroup>,
    allBarnsBounds: List<BarnUi>,
    zoomLevel: Float,
    onGroupClick: (BarnGroup) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    // Lighter, subtle grid
    val gridColor = Color.Black.copy(alpha = 0.05f) 
    val clusterColor = MaterialTheme.colorScheme.tertiary
    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.labelSmall.copy(color = onPrimaryColor, fontWeight = FontWeight.Bold)
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (allBarnsBounds.isEmpty()) return@Canvas
        
        val minLat = allBarnsBounds.minOfOrNull { it.lat } ?: 0.0
        val maxLat = allBarnsBounds.maxOfOrNull { it.lat } ?: 1.0
        val minLng = allBarnsBounds.minOfOrNull { it.lng } ?: 0.0
        val maxLng = allBarnsBounds.maxOfOrNull { it.lng } ?: 1.0
        
        val w = this.size.width
        val h = this.size.height
        
        // Draw Grid
        drawGrid(this, w, h, gridColor)
        
        groups.forEach { group ->
            val nx = if (maxLng != minLng) ((group.centerLng - minLng) / (maxLng - minLng)).toFloat() else 0.5f
            val ny = if (maxLat != minLat) (1f - ((group.centerLat - minLat) / (maxLat - minLat)).toFloat()) else 0.5f
            val x = nx * w
            val y = ny * h
            
            if (group.barns.size == 1) {
                drawPin(
                    scope = this,
                    x = x,
                    y = y,
                    color = primaryColor,
                    centerColor = onPrimaryColor,
                    size = 14.dp.toPx() * zoomLevel // Slightly larger pins
                )
            } else {
                drawClusterPin(
                    scope = this,
                    x = x,
                    y = y,
                    count = group.barns.size,
                    color = clusterColor,
                    textColor = onPrimaryColor,
                    size = 18.dp.toPx() * zoomLevel,
                    textMeasurer = textMeasurer,
                    textStyle = textStyle
                )
            }
        }
    }
}

private fun drawGrid(scope: DrawScope, width: Float, height: Float, gridColor: Color) {
    val gridSize = 60f // Larger grid squares
    
    for (y in 0..(height / gridSize).toInt()) {
        val yPos = y * gridSize
        scope.drawLine(
            color = gridColor,
            start = Offset(0f, yPos),
            end = Offset(width, yPos),
            strokeWidth = 1f
        )
    }
    
    for (x in 0..(width / gridSize).toInt()) {
        val xPos = x * gridSize
        scope.drawLine(
            color = gridColor,
            start = Offset(xPos, 0f),
            end = Offset(xPos, height),
            strokeWidth = 1f
        )
    }
}

private fun drawPin(
    scope: DrawScope,
    x: Float,
    y: Float,
    color: Color,
    centerColor: Color,
    size: Float
) {
    // Drop shadow
    scope.drawCircle(
        color = Color.Black.copy(alpha = 0.2f),
        radius = size * 0.8f,
        center = Offset(x, y + size * 0.5f)
    )

    // Pin Body
    scope.drawCircle(
        color = color,
        radius = size,
        center = Offset(x, y - size * 0.3f)
    )
    
    // Pin Center (White dot)
    scope.drawCircle(
        color = centerColor,
        radius = size * 0.4f,
        center = Offset(x, y - size * 0.3f)
    )
    
    // Stroke
    scope.drawCircle(
        color = Color.White,
        radius = size,
        center = Offset(x, y - size * 0.3f),
        style = Stroke(width = size * 0.1f)
    )
}

private fun drawClusterPin(
    scope: DrawScope,
    x: Float,
    y: Float,
    count: Int,
    color: Color,
    textColor: Color,
    size: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    textStyle: TextStyle
) {
    // Shadow
    scope.drawCircle(
        color = Color.Black.copy(alpha = 0.2f),
        radius = size,
        center = Offset(x, y + 2f)
    )

    // Body
    scope.drawCircle(
        color = color,
        radius = size,
        center = Offset(x, y)
    )
    
    // Border
    scope.drawCircle(
        color = Color.White,
        radius = size,
        center = Offset(x, y),
        style = Stroke(width = size * 0.15f)
    )
    
    // Text
    val textLayoutResult = textMeasurer.measure(
        text = count.toString(),
        style = textStyle
    )
    
    scope.drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(
            x - textLayoutResult.size.width / 2,
            y - textLayoutResult.size.height / 2
        )
    )
}

private fun groupBarnsByProximity(
    barns: List<BarnUi>,
    zoomLevel: Float
): List<BarnGroup> {
    val groups = mutableListOf<BarnGroup>()
    val threshold = 0.01 / zoomLevel 
    
    barns.forEach { barn ->
        var addedToGroup = false
        
        for (group in groups) {
            val distance = calculateDistance(barn.lat, barn.lng, group.centerLat, group.centerLng)
            if (distance < threshold) {
                group.barns.add(barn)
                addedToGroup = true
                break
            }
        }
        
        if (!addedToGroup) {
            groups.add(BarnGroup(barn.lat, barn.lng, mutableListOf(barn)))
        }
    }
    return groups 
}

private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    return Math.sqrt(Math.pow(lat1 - lat2, 2.0) + Math.pow(lng1 - lng2, 2.0))
}

private data class BarnGroup(
    val centerLat: Double,
    val centerLng: Double,
    val barns: MutableList<BarnUi>
)

@Composable
private fun BarnListItemHorizontal(
    barn: BarnUi,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(280.dp)
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .size(80.dp)
                    .aspectRatio(1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = barn.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.LocationOn, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = barn.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star, 
                        contentDescription = null, 
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "4.8 (120)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
