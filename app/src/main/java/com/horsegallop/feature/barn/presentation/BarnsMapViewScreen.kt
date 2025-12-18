package com.horsegallop.feature.barn.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.horsegallop.core.R
import com.horsegallop.feature.barn.domain.model.BarnUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarnsMapViewScreen(
    navController: NavController,
    viewModel: BarnViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val barns = uiState.filteredBarns
    
    // Zoom seviyesi state
    var zoomLevel by remember { mutableStateOf(1f) }
    val maxZoom = 3f
    val minZoom = 0.5f
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Barns") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Harita görünümü
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    // Harita çizimi
                    BarnsMapCanvas(
                        barns = barns.map { it.barn },
                        zoomLevel = zoomLevel,
                        onBarnClick = { barn ->
                            // Barn detayına git
                            navController.navigate("barnDetail/${barn.id}")
                        }
                    )
                    
                    // Zoom kontrolleri
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Zoom in
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            IconButton(
                                onClick = { zoomLevel = (zoomLevel * 1.2f).coerceAtMost(maxZoom) },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Zoom in",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        // Zoom out
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            IconButton(
                                onClick = { zoomLevel = (zoomLevel / 1.2f).coerceAtLeast(minZoom) },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    Icons.Filled.Remove,
                                    contentDescription = "Zoom out",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    
                    // Grup bilgisi
                    val groupedBarns = groupBarnsByProximity(barns.map { it.barn }, zoomLevel)
                    if (groupedBarns.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Groups,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "${groupedBarns.size} barns visible",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            
            // Barn listesi alt panel
            if (barns.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Nearby Barns",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        // İlk 3 barn göster
                        barns.take(3).forEach { barn ->
                            BarnListItemCompact(
                                barn = barn.barn,
                                onClick = { navController.navigate("barnDetail/${barn.barn.id}") }
                            )
                        }
                        
                        if (barns.size > 3) {
                            TextButton(
                                onClick = { navController.navigate(Dest.Barns.route) },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("View all ${barns.size} barns")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BarnsMapCanvas(
    barns: List<BarnUi>,
    zoomLevel: Float,
    onBarnClick: (BarnUi) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (barns.isEmpty()) return@Canvas
        
        val minLat = barns.minOfOrNull { it.lat } ?: 0.0
        val maxLat = barns.maxOfOrNull { it.lat } ?: 1.0
        val minLng = barns.minOfOrNull { it.lng } ?: 0.0
        val maxLng = barns.maxOfOrNull { it.lng } ?: 1.0
        
        val w = this.size.width
        val h = this.size.height
        
        // Arka plan ızgara
        drawGrid(this, w, h, gridColor)
        
        barns.forEach { item ->
            val nx = if (maxLng != minLng) ((item.lng - minLng) / (maxLng - minLng)).toFloat() else 0.5f
            val ny = if (maxLat != minLat) (1f - ((item.lat - minLat) / (maxLat - minLat)).toFloat()) else 0.5f
            val x = nx * w
            val y = ny * h
            
            // Pin çizimi
            drawPin(
                scope = this,
                x = x,
                y = y,
                color = primaryColor,
                centerColor = onPrimaryColor,
                size = 12.dp.toPx() * zoomLevel
            )
        }
    }
}

private fun drawGrid(scope: DrawScope, width: Float, height: Float, gridColor: Color) {
    val gridSize = 50f
    
    // Yatay çizgiler
    for (y in 0..(height / gridSize).toInt()) {
        val yPos = y * gridSize
        scope.drawLine(
            color = gridColor,
            start = androidx.compose.ui.geometry.Offset(0f, yPos),
            end = androidx.compose.ui.geometry.Offset(width, yPos),
            strokeWidth = 1f
        )
    }
    
    // Dikey çizgiler
    for (x in 0..(width / gridSize).toInt()) {
        val xPos = x * gridSize
        scope.drawLine(
            color = gridColor,
            start = androidx.compose.ui.geometry.Offset(xPos, 0f),
            end = androidx.compose.ui.geometry.Offset(xPos, height),
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
    // Pin gövdesi
    scope.drawCircle(
        color = color,
        radius = size,
        center = androidx.compose.ui.geometry.Offset(x, y - size * 0.3f)
    )
    
    // Pin merkezi
    scope.drawCircle(
        color = centerColor,
        radius = size * 0.4f,
        center = androidx.compose.ui.geometry.Offset(x, y - size * 0.3f)
    )
    
    // Pin gölgesi
    scope.drawCircle(
        color = color.copy(alpha = 0.3f),
        radius = size * 0.8f,
        center = androidx.compose.ui.geometry.Offset(x + 2f, y + 2f)
    )
}

private fun groupBarnsByProximity(
    barns: List<BarnUi>,
    zoomLevel: Float
): List<BarnGroup> {
    val groups = mutableListOf<BarnGroup>()
    val threshold = 0.01 / zoomLevel // Yakınlık eşiği
    
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
    
    return groups.filter { it.barns.size > 1 } // Sadece birden fazla barn içeren gruplar
}

private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    // Basit mesafe hesaplama (daha karmaşık algoritma kullanılabilir)
    return Math.sqrt(Math.pow(lat1 - lat2, 2.0) + Math.pow(lng1 - lng2, 2.0))
}

private data class BarnGroup(
    val centerLat: Double,
    val centerLng: Double,
    val barns: MutableList<BarnUi>
)

@Composable
private fun BarnListItemCompact(
    barn: BarnUi,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = barn.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Text(
                        text = barn.location,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                contentDescription = "View",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}