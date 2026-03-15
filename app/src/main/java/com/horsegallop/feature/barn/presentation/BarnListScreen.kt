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
import com.horsegallop.navigation.Dest
import com.horsegallop.core.components.ViewAllButton
import com.horsegallop.core.components.HorseGallopSearchBar
import com.horsegallop.domain.barn.model.BarnUi
import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.ui.theme.LocalSemanticColors



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

  Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp)) {
      Spacer(modifier = Modifier.height(24.dp))
      com.horsegallop.core.components.HorseGallopSearchBar(
        query = uiState.query,
        onQueryChange = viewModel::updateQuery,
        placeholder = stringResource(com.horsegallop.R.string.barn_search_placeholder),
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
            items(uiState.availableFilters) { filterKey ->
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
        horizontalArrangement = Arrangement.End
      ) {
        com.horsegallop.core.components.ViewAllButton(
          onClick = { navController?.navigate(Dest.BarnsMapView.route) }
        )
      }
      

      
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
              text = uiState.error ?: stringResource(com.horsegallop.R.string.backend_error_generic),
              style = MaterialTheme.typography.bodyMedium,
              color = semantic.calloutOnContainer,
              textAlign = TextAlign.Center
            )
            OutlinedButton(onClick = { viewModel.loadBarns() }) {
              Text(text = stringResource(com.horsegallop.R.string.retry))
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
                if (hasActiveSearch) com.horsegallop.R.string.barn_empty_title
                else com.horsegallop.R.string.barn_no_barns_title
              ),
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            )
            Text(
              text = stringResource(
                if (hasActiveSearch) com.horsegallop.R.string.barn_empty_subtitle
                else com.horsegallop.R.string.barn_no_barns_subtitle
              ),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
            if (hasActiveFilters) {
              OutlinedButton(onClick = viewModel::clearFilters) {
                Text(
                  text = stringResource(com.horsegallop.R.string.barn_clear_filters),
                  color = MaterialTheme.colorScheme.primary,
                  style = MaterialTheme.typography.labelLarge
                )
              }
            } else {
              OutlinedButton(onClick = { viewModel.loadBarns() }) {
                Text(
                  text = stringResource(com.horsegallop.R.string.retry),
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
            items(uiState.filteredBarns) { barnWithLocation ->
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
                        contentDescription = if (barn.isFavorite) "Remove from favorites" else "Add to favorites",
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
                    items(barn.tags) { tag ->
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

@Composable
fun getFilterLabel(key: String): String {
    return when(key) {
        "cafe" -> stringResource(com.horsegallop.R.string.barn_filter_cafe)
        "indoor_arena" -> stringResource(com.horsegallop.R.string.barn_filter_indoor_arena)
        "outdoor_arena" -> stringResource(com.horsegallop.R.string.barn_filter_outdoor_arena)
        "parking" -> stringResource(com.horsegallop.R.string.barn_filter_parking)
        "lessons" -> stringResource(com.horsegallop.R.string.barn_filter_lessons)
        "boarding" -> stringResource(com.horsegallop.R.string.barn_filter_boarding)
        "vet" -> stringResource(com.horsegallop.R.string.barn_filter_vet)
        "farrier" -> stringResource(com.horsegallop.R.string.barn_filter_farrier)
        "lighting" -> stringResource(com.horsegallop.R.string.barn_filter_lighting)
        "trail" -> stringResource(com.horsegallop.R.string.barn_filter_trail)
        "open_now" -> stringResource(com.horsegallop.R.string.barn_filter_open_now)
        else -> key
    }
}
