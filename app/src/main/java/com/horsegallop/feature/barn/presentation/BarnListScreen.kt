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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.horsegallop.feature.barn.domain.model.BarnUi


@Composable
fun BarnListScreen(
  onBarnClick: (BarnUi) -> Unit,
  onHomeClick: () -> Unit = {},
  onRideClick: () -> Unit = {},
  navController: NavController? = null,
  viewModel: BarnViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsState()

  Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp)) {
      // Modern search bar with rounded corners and better spacing
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp, bottom = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            Icons.Filled.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
          ) {
            if (uiState.query.isEmpty()) {
              Text(
                text = stringResource(com.horsegallop.core.R.string.barn_search_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
              )
            }
            androidx.compose.foundation.text.BasicTextField(
              value = uiState.query,
              onValueChange = viewModel::updateQuery,
              textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
              ),
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
          }
          if (uiState.query.isNotBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
              onClick = viewModel::clearQuery,
              modifier = Modifier.size(24.dp)
            ) {
              Icon(
                Icons.Filled.Remove,
                contentDescription = "Clear",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
            }
          }

        }
      }
      
      if (uiState.availableFilters.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))

        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
        Box(modifier = Modifier.fillMaxWidth()) {
          LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(uiState.availableFilters) { filterKey ->
              val selected: Boolean = uiState.selectedFilters.contains(filterKey)
              val label = getFilterLabel(filterKey)
              Surface(
                selected = selected,
                onClick = { viewModel.toggleFilter(filterKey) },
                shape = RoundedCornerShape(20.dp),
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
      Spacer(modifier = Modifier.height(16.dp))
      
      // Map başlığı ve View All butonu
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = stringResource(com.horsegallop.core.R.string.barn_map_title),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(
          onClick = { navController?.navigate("barnsMapView") },
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("View All", color = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            Icons.Filled.ArrowForward,
            contentDescription = "View all barns",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
          )
        }
      }
      
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth().height(200.dp) // Sabit yükseklik
      ) {
        Box(
          modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
          contentAlignment = Alignment.TopStart
        ) {
          val primaryColor = MaterialTheme.colorScheme.primary
          val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
          val filtered = uiState.filteredBarns
          
          Canvas(modifier = Modifier.fillMaxSize()) {
            // Simplified drawing to avoid jank
            val minLat = filtered.minOfOrNull { it.lat } ?: 0.0
            val maxLat = filtered.maxOfOrNull { it.lat } ?: 1.0
            val minLng = filtered.minOfOrNull { it.lng } ?: 0.0
            val maxLng = filtered.maxOfOrNull { it.lng } ?: 1.0
            val w = this.size.width
            val h = this.size.height
            filtered.forEach { item ->
              val nx = if (maxLng != minLng) ((item.lng - minLng) / (maxLng - minLng)).toFloat() else 0.5f
              val ny = if (maxLat != minLat) (1f - ((item.lat - minLat) / (maxLat - minLat)).toFloat()) else 0.5f
              val x = nx * w
              val y = ny * h
              // Tail + dot pin
              drawLine(
                color = primaryColor,
                start = Offset(x, y - 8.dp.toPx()),
                end = Offset(x, y + 10.dp.toPx()),
                strokeWidth = 4f
              )
              drawCircle(
                color = primaryColor,
                radius = 8.dp.toPx(),
                center = Offset(x, y - 12.dp.toPx())
              )
              drawCircle(
                color = onPrimaryColor,
                radius = 3.dp.toPx(),
                center = Offset(x, y - 12.dp.toPx())
              )
            }
          }
          Row(
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = stringResource(com.horsegallop.core.R.string.barn_map_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ) {
              Row {
                IconButton(onClick = { /* zoom out placeholder */ }, modifier = Modifier.size(32.dp)) {
                  Icon(Icons.Filled.Remove, contentDescription = "Zoom out", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = { /* zoom in placeholder */ }, modifier = Modifier.size(32.dp)) {
                  Icon(Icons.Filled.Add, contentDescription = "Zoom in", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
              }
            }
          }
          Text(
            text = if (uiState.query.isBlank()) "${stringResource(com.horsegallop.core.R.string.barn_results_prefix)}: ${filtered.size}" else stringResource(com.horsegallop.core.R.string.barn_search_results, filtered.size),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
          )
        }
      }
      if (uiState.filteredBarns.isEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
              text = stringResource(com.horsegallop.core.R.string.barn_empty_title),
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            )
            Text(
              text = stringResource(com.horsegallop.core.R.string.barn_empty_subtitle),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              OutlinedButton(onClick = viewModel::clearFilters) {
                Text(text = stringResource(com.horsegallop.core.R.string.barn_clear_filters), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
              }
            }
          }
        }
      } else {
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp, top = 8.dp)
        ) {
            items(uiState.filteredBarns) { barnWithLocation ->
                BarnCard(barn = barnWithLocation.barn, onClick = { onBarnClick(barnWithLocation.barn) })
            }
        }
      }
    }
}

@Composable
fun BarnCard(barn: BarnUi, onClick: () -> Unit) {
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Enhanced icon with gradient background
                Box(
                    modifier = Modifier
                        .size(56.dp)
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
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = barn.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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
                    }
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
                    items(barn.tags) { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
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
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = getFilterLabel(tag),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
fun getFilterLabel(key: String): String {
    return when(key) {
        "cafe" -> stringResource(com.horsegallop.core.R.string.barn_filter_cafe)
        "indoor_arena" -> stringResource(com.horsegallop.core.R.string.barn_filter_indoor_arena)
        "outdoor_arena" -> stringResource(com.horsegallop.core.R.string.barn_filter_outdoor_arena)
        "parking" -> stringResource(com.horsegallop.core.R.string.barn_filter_parking)
        "lessons" -> stringResource(com.horsegallop.core.R.string.barn_filter_lessons)
        "boarding" -> stringResource(com.horsegallop.core.R.string.barn_filter_boarding)
        "vet" -> stringResource(com.horsegallop.core.R.string.barn_filter_vet)
        "farrier" -> stringResource(com.horsegallop.core.R.string.barn_filter_farrier)
        "lighting" -> stringResource(com.horsegallop.core.R.string.barn_filter_lighting)
        "trail" -> stringResource(com.horsegallop.core.R.string.barn_filter_trail)
        "open_now" -> stringResource(com.horsegallop.core.R.string.barn_filter_open_now)
        else -> key
    }
}
