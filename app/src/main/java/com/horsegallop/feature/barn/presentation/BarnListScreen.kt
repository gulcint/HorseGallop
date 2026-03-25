package com.horsegallop.feature.barn.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.drawText
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.horsegallop.navigation.Dest
import com.horsegallop.core.components.HorseGallopSearchBar
import com.horsegallop.domain.barn.model.BarnUi
import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.R
import com.horsegallop.ui.theme.LocalSemanticColors

enum class BarnViewMode { LIST, MAP }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarnListScreen(
  onBarnClick: (BarnUi) -> Unit,
  onHomeClick: () -> Unit = {},
  onRideClick: () -> Unit = {},
  navController: NavController? = null,
  viewModel: BarnViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsState()
  val semantic = LocalSemanticColors.current
  var viewMode by remember { mutableStateOf(BarnViewMode.LIST) }

  Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp)) {
      Spacer(modifier = Modifier.height(24.dp))
      HorseGallopSearchBar(
        query = uiState.query,
        onQueryChange = viewModel::updateQuery,
        placeholder = stringResource(R.string.barn_search_placeholder),
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp, bottom = 12.dp)
      )

      if (uiState.availableFilters.isNotEmpty()) {
        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
        Box(modifier = Modifier.fillMaxWidth()) {
          LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(uiState.availableFilters, key = { it }) { filterKey ->
              val selected: Boolean = uiState.selectedFilters.contains(filterKey)
              val label = getFilterLabel(filterKey)
              Surface(
                selected = selected,
                onClick = { viewModel.toggleFilter(filterKey) },
                shape = RoundedCornerShape(20.dp),
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.height(36.dp)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  if (selected) {
                    Icon(
                      Icons.Filled.Check,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = {
            viewMode = if (viewMode == BarnViewMode.LIST) BarnViewMode.MAP else BarnViewMode.LIST
          }
        ) {
          Icon(
            imageVector = if (viewMode == BarnViewMode.LIST) Icons.Filled.Map else Icons.AutoMirrored.Filled.List,
            contentDescription = stringResource(
              if (viewMode == BarnViewMode.LIST) R.string.barn_view_map_cd else R.string.barn_view_list_cd
            ),
            tint = MaterialTheme.colorScheme.primary
          )
        }
      }

      when (viewMode) {
        BarnViewMode.MAP -> {
          BarnInlineMapContent(
            barns = uiState.filteredBarns,
            onBarnClick = { barn -> onBarnClick(barn) },
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
          )
        }
        BarnViewMode.LIST -> {
          if (uiState.loading) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
              contentAlignment = Alignment.Center
            ) {
              CircularProgressIndicator()
            }
          } else if (uiState.error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
              shape = RoundedCornerShape(24.dp),
              colors = CardDefaults.cardColors(containerColor = semantic.calloutErrorContainer),
              border = BorderStroke(1.dp, semantic.calloutBorderError),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp)
              ) {
                Text(
                  text = uiState.error ?: stringResource(R.string.backend_error_generic),
                  style = MaterialTheme.typography.bodyMedium,
                  color = semantic.calloutOnContainer,
                  textAlign = TextAlign.Center
                )
                OutlinedButton(onClick = { viewModel.loadBarns() }) {
                  Text(text = stringResource(R.string.retry))
                }
              }
            }
          } else if (uiState.filteredBarns.isEmpty()) {
            val hasActiveFilters = uiState.selectedFilters.isNotEmpty()
            val hasActiveSearch = hasActiveFilters || uiState.query.isNotBlank()
            Spacer(modifier = Modifier.height(12.dp))
            Card(
              shape = RoundedCornerShape(24.dp),
              colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
              border = BorderStroke(1.dp, semantic.cardStroke),
              elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp)
              ) {
                Surface(
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                  modifier = Modifier.size(64.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      Icons.Filled.Search,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(28.dp)
                    )
                  }
                }
                Text(
                  text = stringResource(
                    if (hasActiveSearch) R.string.barn_empty_title
                    else R.string.barn_no_barns_title
                  ),
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Bold,
                  textAlign = TextAlign.Center
                )
                Text(
                  text = stringResource(
                    if (hasActiveSearch) R.string.barn_empty_subtitle
                    else R.string.barn_no_barns_subtitle
                  ),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center
                )
                if (hasActiveFilters) {
                  OutlinedButton(onClick = viewModel::clearFilters) {
                    Text(
                      text = stringResource(R.string.barn_clear_filters),
                      color = MaterialTheme.colorScheme.primary,
                      style = MaterialTheme.typography.labelLarge
                    )
                  }
                } else {
                  OutlinedButton(onClick = { viewModel.loadBarns() }) {
                    Text(
                      text = stringResource(R.string.retry),
                      color = MaterialTheme.colorScheme.primary,
                      style = MaterialTheme.typography.labelLarge
                    )
                  }
                }
              }
            }
          } else {
            Spacer(modifier = Modifier.height(4.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp, top = 8.dp)
            ) {
                items(uiState.filteredBarns, key = { it.barn.id }) { barnWithLocation ->
                    BarnCard(
                        barn = barnWithLocation.barn,
                        distanceKm = barnWithLocation.distanceKm.takeIf { it < Double.MAX_VALUE },
                        onClick = { onBarnClick(barnWithLocation.barn) },
                        onFavoriteClick = { viewModel.toggleFavorite(barnWithLocation.barn.id) }
                    )
                }
            }
          }
        }
      }
    }
}

@Composable
private fun BarnInlineMapContent(
    barns: List<BarnWithLocation>,
    onBarnClick: (BarnUi) -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val barnsWithCoords = remember(barns) {
        barns.filter { it.barn.lat != 0.0 || it.barn.lng != 0.0 }
    }
    var zoomLevel by remember { mutableStateOf(1f) }
    val maxZoom = 3f
    val minZoom = 0.5f
    val groupedBarns = remember(barnsWithCoords, zoomLevel) {
        groupBarnsByProximityInline(barnsWithCoords.map { it.barn }, zoomLevel)
    }

    Box(modifier = modifier.background(semantic.screenBase)) {
        BarnInlineMapCanvas(
            groups = groupedBarns,
            allBarnsBounds = barnsWithCoords.map { it.barn },
            zoomLevel = zoomLevel,
            onGroupClick = { group ->
                if (group.barns.size == 1) {
                    onBarnClick(group.barns.first())
                } else {
                    zoomLevel = (zoomLevel * 1.5f).coerceAtMost(maxZoom)
                }
            }
        )

        // Zoom Controls
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = semantic.panelOverlay,
                shadowElevation = 4.dp,
                modifier = Modifier.width(48.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { zoomLevel = (zoomLevel * 1.2f).coerceAtMost(maxZoom) }
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { zoomLevel = (zoomLevel / 1.2f).coerceAtLeast(minZoom) }
                    ) {
                        Icon(
                            Icons.Filled.Remove,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Bottom horizontal barn list
        if (barnsWithCoords.isNotEmpty()) {
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                items(barnsWithCoords.take(10), key = { it.barn.id }) { barnWithLocation ->
                    BarnMapCard(
                        barn = barnWithLocation.barn,
                        onClick = { onBarnClick(barnWithLocation.barn) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BarnMapCard(
    barn: BarnUi,
    onClick: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(220.dp)
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = BorderStroke(1.dp, semantic.cardStroke),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = barn.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = barn.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun BarnInlineMapCanvas(
    groups: List<InlineBarnGroup>,
    allBarnsBounds: List<BarnUi>,
    zoomLevel: Float,
    onGroupClick: (InlineBarnGroup) -> Unit
) {
    val semantic = LocalSemanticColors.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val gridColor = semantic.mapGrid
    val clusterColor = MaterialTheme.colorScheme.secondary
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.labelSmall.copy(
        color = onPrimaryColor,
        fontWeight = FontWeight.Bold
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) { /* handled per group below */ }
    ) {
        val w = this.size.width
        val h = this.size.height
        drawInlineGrid(this, w, h, gridColor)
        if (allBarnsBounds.isEmpty()) return@Canvas

        val minLat = allBarnsBounds.minOfOrNull { it.lat } ?: 0.0
        val maxLat = allBarnsBounds.maxOfOrNull { it.lat } ?: 1.0
        val minLng = allBarnsBounds.minOfOrNull { it.lng } ?: 0.0
        val maxLng = allBarnsBounds.maxOfOrNull { it.lng } ?: 1.0
        val centerXNorm = 0.5f
        val centerYNorm = 0.5f

        groups.forEach { group ->
            val nx = if (maxLng != minLng) ((group.centerLng - minLng) / (maxLng - minLng)).toFloat() else 0.5f
            val ny = if (maxLat != minLat) (1f - ((group.centerLat - minLat) / (maxLat - minLat)).toFloat()) else 0.5f
            val zoomedNx = ((nx - centerXNorm) * zoomLevel + centerXNorm).coerceIn(0f, 1f)
            val zoomedNy = ((ny - centerYNorm) * zoomLevel + centerYNorm).coerceIn(0f, 1f)
            val x = zoomedNx * w
            val y = zoomedNy * h

            if (group.barns.size == 1) {
                drawInlinePin(
                    scope = this,
                    x = x,
                    y = y,
                    color = primaryColor,
                    centerColor = onPrimaryColor,
                    size = 14.dp.toPx(),
                    shadowColor = semantic.imageOverlaySoft.copy(alpha = 0.20f),
                    borderColor = semantic.onImageOverlay
                )
            } else {
                drawInlineClusterPin(
                    scope = this,
                    x = x,
                    y = y,
                    count = group.barns.size,
                    color = clusterColor,
                    textColor = onPrimaryColor,
                    size = 18.dp.toPx(),
                    textMeasurer = textMeasurer,
                    textStyle = textStyle,
                    shadowColor = semantic.imageOverlaySoft.copy(alpha = 0.20f),
                    borderColor = semantic.onImageOverlay
                )
            }
        }
    }
}

private fun drawInlineGrid(scope: DrawScope, width: Float, height: Float, gridColor: Color) {
    val gridSize = 60f
    for (y in 0..(height / gridSize).toInt()) {
        val yPos = y * gridSize
        scope.drawLine(color = gridColor, start = Offset(0f, yPos), end = Offset(width, yPos), strokeWidth = 1f)
    }
    for (x in 0..(width / gridSize).toInt()) {
        val xPos = x * gridSize
        scope.drawLine(color = gridColor, start = Offset(xPos, 0f), end = Offset(xPos, height), strokeWidth = 1f)
    }
}

private fun drawInlinePin(
    scope: DrawScope,
    x: Float,
    y: Float,
    color: Color,
    centerColor: Color,
    size: Float,
    shadowColor: Color,
    borderColor: Color
) {
    scope.drawCircle(color = shadowColor, radius = size * 0.8f, center = Offset(x, y + size * 0.5f))
    scope.drawCircle(color = color, radius = size, center = Offset(x, y - size * 0.3f))
    scope.drawCircle(color = centerColor, radius = size * 0.4f, center = Offset(x, y - size * 0.3f))
    scope.drawCircle(
        color = borderColor,
        radius = size,
        center = Offset(x, y - size * 0.3f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = size * 0.1f)
    )
}

private fun drawInlineClusterPin(
    scope: DrawScope,
    x: Float,
    y: Float,
    count: Int,
    color: Color,
    textColor: Color,
    size: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    textStyle: androidx.compose.ui.text.TextStyle,
    shadowColor: Color,
    borderColor: Color
) {
    scope.drawCircle(color = shadowColor, radius = size, center = Offset(x, y + 2f))
    scope.drawCircle(color = color, radius = size, center = Offset(x, y))
    scope.drawCircle(
        color = borderColor,
        radius = size,
        center = Offset(x, y),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = size * 0.15f)
    )
    val textLayoutResult = textMeasurer.measure(text = count.toString(), style = textStyle)
    scope.drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(x - textLayoutResult.size.width / 2, y - textLayoutResult.size.height / 2)
    )
}

private fun groupBarnsByProximityInline(
    barns: List<BarnUi>,
    zoomLevel: Float
): List<InlineBarnGroup> {
    val groups = mutableListOf<InlineBarnGroup>()
    val threshold = 0.01 / zoomLevel
    barns.forEach { barn ->
        var addedToGroup = false
        for (group in groups) {
            val dx = barn.lat - group.centerLat
            val dy = barn.lng - group.centerLng
            val distance = Math.sqrt(dx * dx + dy * dy)
            if (distance < threshold) {
                group.barns.add(barn)
                addedToGroup = true
                break
            }
        }
        if (!addedToGroup) {
            groups.add(InlineBarnGroup(barn.lat, barn.lng, mutableListOf(barn)))
        }
    }
    return groups
}

private data class InlineBarnGroup(
    val centerLat: Double,
    val centerLng: Double,
    val barns: MutableList<BarnUi>
)

@Composable
fun BarnCard(barn: BarnUi, distanceKm: Double? = null, onClick: () -> Unit, onFavoriteClick: () -> Unit) {
    val semantic = LocalSemanticColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = BorderStroke(1.dp, semantic.cardStroke),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Enhanced icon with gradient background
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Home,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = barn.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = barn.location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (distanceKm != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "· ${if (distanceKm < 1.0) "${(distanceKm * 1000).toInt()} m" else "${distanceKm.toInt()} km"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (barn.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = if (barn.isFavorite) stringResource(R.string.cd_favorite_remove) else stringResource(R.string.cd_favorite_add),
                        tint = if (barn.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            if (barn.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(barn.tags, key = { it }) { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.82f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.38f)
                            ),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = getFilterLabel(tag),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BarnListScreenPreview() {
    MaterialTheme {
        BarnCard(
            barn = BarnUi(
                id = "1",
                name = "Ankara Hipodromu",
                description = "Modern at binicilik merkezi",
                location = "Ankara, Türkiye",
                tags = listOf("indoor_arena", "lessons"),
                rating = 4.8,
                reviewCount = 120
            ),
            distanceKm = 3.2,
            onClick = {},
            onFavoriteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BarnInlineMapContentPreview() {
    MaterialTheme {
        BarnInlineMapContent(
            barns = listOf(
                BarnWithLocation(
                    barn = BarnUi(
                        id = "1",
                        name = "İstanbul Atlı Spor",
                        description = "Şehrin kalbinde",
                        location = "Beşiktaş, İstanbul",
                        lat = 41.0,
                        lng = 29.0,
                        rating = 4.7,
                        reviewCount = 85
                    ),
                    lat = 41.0,
                    lng = 29.0,
                    amenities = emptySet(),
                    distanceKm = 2.0
                )
            ),
            onBarnClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        )
    }
}

@Composable
fun getFilterLabel(key: String): String {
    return when(key) {
        "cafe" -> stringResource(R.string.barn_filter_cafe)
        "indoor_arena" -> stringResource(R.string.barn_filter_indoor_arena)
        "outdoor_arena" -> stringResource(R.string.barn_filter_outdoor_arena)
        "parking" -> stringResource(R.string.barn_filter_parking)
        "lessons" -> stringResource(R.string.barn_filter_lessons)
        "boarding" -> stringResource(R.string.barn_filter_boarding)
        "vet" -> stringResource(R.string.barn_filter_vet)
        "farrier" -> stringResource(R.string.barn_filter_farrier)
        "lighting" -> stringResource(R.string.barn_filter_lighting)
        "trail" -> stringResource(R.string.barn_filter_trail)
        "open_now" -> stringResource(R.string.barn_filter_open_now)
        else -> key
    }
}
