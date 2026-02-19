@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.horsegallop.feature.ride.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Stop
 
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import dagger.hilt.android.lifecycle.HiltViewModel
import com.horsegallop.domain.ride.model.GeoPoint
import com.horsegallop.domain.ride.model.RideSession
import com.horsegallop.domain.ride.repository.RideHistoryRepository
import com.horsegallop.domain.ride.usecase.ObserveIsRidingUseCase
import com.horsegallop.domain.ride.usecase.ObserveRideMetricsUseCase
import com.horsegallop.domain.ride.usecase.SetAutoDetectUseCase
import com.horsegallop.domain.ride.usecase.StartRideUseCase
import com.horsegallop.domain.ride.usecase.StopRideUseCase
import com.horsegallop.domain.barn.repository.BarnRepository
import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.domain.auth.usecase.GetUserProfileUseCase
import com.horsegallop.domain.auth.usecase.GetCurrentUserIdUseCase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.OutlinedTextField
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults

data class RideUiState(
  val speedKmh: Float,
  val distanceKm: Float,
  val durationSec: Int,
  val calories: Int,
  val isRiding: Boolean,
  val autoDetect: Boolean,
  val dailyTrend: List<Float>,
  val weeklyTrend: List<Float>,
  val totalCareerDistance: Float = 0f,
  val pathPoints: List<GeoPoint>,
  val barns: List<BarnWithLocation> = emptyList(),
  val selectedBarn: BarnWithLocation? = null
)

@HiltViewModel
class RideTrackingViewModel @Inject constructor(
    private val startRideUseCase: StartRideUseCase,
    private val stopRideUseCase: StopRideUseCase,
    private val observeRideMetricsUseCase: ObserveRideMetricsUseCase,
    private val observeIsRidingUseCase: ObserveIsRidingUseCase,
    private val setAutoDetectUseCase: SetAutoDetectUseCase,
    private val rideHistoryRepository: RideHistoryRepository,
    private val barnRepository: BarnRepository,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) : ViewModel() {
  private val _uiState: MutableStateFlow<RideUiState> = MutableStateFlow(
    RideUiState(
      speedKmh = 0f,
      distanceKm = 0f,
      durationSec = 0,
      calories = 0,
      isRiding = false,
      autoDetect = false,
      dailyTrend = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
      weeklyTrend = listOf(0f, 0f, 0f, 0f),
      pathPoints = listOf(GeoPoint(41.0, 29.0))
    )
  )
  val uiState: StateFlow<RideUiState> = _uiState
  private var userWeightKg: Float = 70f

  init {
      combine(
          observeIsRidingUseCase(),
          observeRideMetricsUseCase()
      ) { isRiding, metrics ->
          _uiState.value.copy(
              isRiding = isRiding,
              speedKmh = metrics.speedKmh,
              distanceKm = metrics.distanceKm,
              durationSec = metrics.durationSec,
              calories = metrics.calories,
              pathPoints = metrics.pathPoints
          )
      }.onEach { newState ->
          _uiState.value = newState
      }.launchIn(viewModelScope)

      barnRepository.getBarns().onEach { barns ->
          _uiState.update { it.copy(barns = barns) }
      }.launchIn(viewModelScope)

      loadUserProfile()

      rideHistoryRepository.getRideHistory().onEach { history ->
          val daily = calculateDailyTrend(history)
          val weekly = calculateWeeklyTrend(history)
          val total = history.sumOf { it.distanceKm.toDouble() }.toFloat()
          _uiState.update { it.copy(
              dailyTrend = daily, 
              weeklyTrend = weekly,
              totalCareerDistance = total
          ) }
      }.launchIn(viewModelScope)
  }

  private fun calculateDailyTrend(history: List<RideSession>): List<Float> {
      val cal = java.util.Calendar.getInstance()
      cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
      cal.set(java.util.Calendar.MINUTE, 0)
      cal.set(java.util.Calendar.SECOND, 0)
      cal.set(java.util.Calendar.MILLISECOND, 0)
      val todayMillis = cal.timeInMillis
      val oneDayMillis = 24 * 60 * 60 * 1000L
      
      val days = FloatArray(7)
      
      history.forEach { ride ->
          val rideCal = java.util.Calendar.getInstance()
          rideCal.timeInMillis = ride.dateMillis
          rideCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
          rideCal.set(java.util.Calendar.MINUTE, 0)
          rideCal.set(java.util.Calendar.SECOND, 0)
          rideCal.set(java.util.Calendar.MILLISECOND, 0)
          
          val diff = todayMillis - rideCal.timeInMillis
          if (diff >= 0) {
              val dayDiff = (diff / oneDayMillis).toInt()
              if (dayDiff in 0..6) {
                  days[6 - dayDiff] += ride.distanceKm
              }
          }
      }
      return days.toList()
  }

  private fun calculateWeeklyTrend(history: List<RideSession>): List<Float> {
      val cal = java.util.Calendar.getInstance()
      cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
      cal.set(java.util.Calendar.MINUTE, 0)
      cal.set(java.util.Calendar.SECOND, 0)
      cal.set(java.util.Calendar.MILLISECOND, 0)
      val todayMillis = cal.timeInMillis
      val oneWeekMillis = 7 * 24 * 60 * 60 * 1000L
      
      val weeks = FloatArray(4)
      
      history.forEach { ride ->
          val rideCal = java.util.Calendar.getInstance()
          rideCal.timeInMillis = ride.dateMillis
          rideCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
          rideCal.set(java.util.Calendar.MINUTE, 0)
          rideCal.set(java.util.Calendar.SECOND, 0)
          rideCal.set(java.util.Calendar.MILLISECOND, 0)
          
          val diff = todayMillis - rideCal.timeInMillis
          if (diff >= 0) {
              val weekDiff = (diff / oneWeekMillis).toInt()
              if (weekDiff in 0..3) {
                  weeks[3 - weekDiff] += ride.distanceKm
              }
          }
      }
      return weeks.toList()
  }

  private fun loadUserProfile() {
      val uid = getCurrentUserIdUseCase() ?: return
      viewModelScope.launch {
          getUserProfileUseCase(uid).collect { result ->
             result.onSuccess { profile ->
                 userWeightKg = profile.weight ?: 70f
             }
          }
      }
  }

  fun selectBarn(barn: BarnWithLocation) {
      _uiState.update { it.copy(selectedBarn = barn) }
  }

  fun toggleRide() {
    viewModelScope.launch {
        if (_uiState.value.isRiding) {
            stopRideUseCase(_uiState.value.selectedBarn?.barn?.name)
        } else {
            startRideUseCase(userWeightKg)
        }
    }
  }

  fun setAutoDetect(enabled: Boolean) {
      viewModelScope.launch {
          setAutoDetectUseCase(enabled)
          _uiState.value = _uiState.value.copy(autoDetect = enabled)
      }
  }
}

@Composable
fun RideTrackingRoute(
    viewModel: RideTrackingViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onHomeClick: () -> Unit = {},
    onBarnsClick: () -> Unit = {}
) {
    RideTrackingScreen(
        viewModel = viewModel,
        onHomeClick = onHomeClick,
        onBarnsClick = onBarnsClick
    )
}

@Composable
@androidx.compose.material3.ExperimentalMaterial3Api
fun RideTrackingScreen(
  viewModel: RideTrackingViewModel,
  onHomeClick: () -> Unit = {},
  onBarnsClick: () -> Unit = {}
) {
  val state: RideUiState by viewModel.uiState.collectAsState()
  
  // Permission Handling
  val locationPermissions = arrayOf(
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION
  )
  
  val launcher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
      // Permissions handled
  }

  LaunchedEffect(Unit) {
      launcher.launch(locationPermissions)
  }
  
  RideTrackingContent(
    state = state,
    onToggleRide = { viewModel.toggleRide() },
    onSetAutoDetect = { viewModel.setAutoDetect(it) },
    onBarnSelected = { viewModel.selectBarn(it) },
    onHomeClick = onHomeClick,
    onBarnsClick = onBarnsClick
  )
}

@Composable
@androidx.compose.material3.ExperimentalMaterial3Api
fun RideTrackingContent(
  state: RideUiState,
  onToggleRide: () -> Unit,
  onSetAutoDetect: (Boolean) -> Unit,
  onBarnSelected: (BarnWithLocation) -> Unit,
  onHomeClick: () -> Unit = {},
  onBarnsClick: () -> Unit = {}
) {
  var selectedRideType: RideType? by remember { mutableStateOf<RideType?>(null) }
  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = { /* No title - greeting moved into content */ },
    floatingActionButton = {
        if (!state.isRiding) {
            ExtendedFloatingActionButton(
                onClick = onToggleRide,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                text = { 
                    Text(
                        text = stringResource(id = com.horsegallop.core.R.string.start_ride),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                }
            )
        }
    },
    floatingActionButtonPosition = FabPosition.Center
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = dimensionResource(id = com.horsegallop.core.R.dimen.padding_screen_horizontal)),
      verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm))
    ) {
      WeatherTopRow(modifier = Modifier.padding(top = 24.dp))
      if (!state.isRiding) {
        BarnSelection(
            barns = state.barns,
            selectedBarn = state.selectedBarn,
            onBarnSelected = onBarnSelected
        )
        RideTypeCard(
          selectedRideType = selectedRideType,
          onRideTypeSelected = { selectedRideType = it }
        )
        Spacer(Modifier.height(dimensionResource(id = com.horsegallop.core.R.dimen.section_spacing_md)))
        StatsOverviewCard(
          dailyDistance = state.dailyTrend.lastOrNull() ?: 0f,
          weeklyDistance = state.weeklyTrend.lastOrNull() ?: 0f,
          totalDistance = state.totalCareerDistance
        )
        Spacer(modifier = Modifier.weight(1f))
        Spacer(Modifier.height(80.dp))
      } else {
        RideMapWithTimer(
          path = state.pathPoints,
          elapsedSec = state.durationSec
        )
        Spacer(Modifier.height(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)))
        StatsRow(speedKmh = state.speedKmh, distanceKm = state.distanceKm, durationSec = state.durationSec)
        Spacer(Modifier.height(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm)))
        CaloriesCard(calories = state.calories)
        Spacer(Modifier.height(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm)))
        ControlsRow(isRiding = state.isRiding, onStop = onToggleRide, autoDetect = state.autoDetect, onToggleAuto = onSetAutoDetect)
      }
    }
  }
}

private enum class RideType(val displayName: String, val emoji: String) {
  DRESSAGE("Dressage", "🐎"),
  SHOW_JUMPING("Show Jumping", "🏇"),
  ENDURANCE("Endurance", "⏱️"),
  TRAIL_RIDING("Trail Riding", "🌲")
}

@Composable
private fun RideTypeCard(
  selectedRideType: RideType?,
  onRideTypeSelected: (RideType) -> Unit
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_xxl))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_md)),
      verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
    ) {
      Text(
        text = stringResource(id = com.horsegallop.core.R.string.ride_type_title),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = stringResource(id = com.horsegallop.core.R.string.ride_type_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      val types = listOf(
        RideType.DRESSAGE, RideType.SHOW_JUMPING,
        RideType.ENDURANCE, RideType.TRAIL_RIDING
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm))
      ) {
        types.take(2).forEach { type ->
          RideTypeChip(
            type = type,
            selected = selectedRideType == type,
            onClick = { onRideTypeSelected(type) },
            modifier = Modifier.weight(1f)
          )
        }
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm))
      ) {
        types.drop(2).forEach { type ->
          RideTypeChip(
            type = type,
            selected = selectedRideType == type,
            onClick = { onRideTypeSelected(type) },
            modifier = Modifier.weight(1f)
          )
        }
      }
    }
  }
}

@Composable
private fun StatsOverviewCard(
  dailyDistance: Float,
  weeklyDistance: Float,
  totalDistance: Float
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_xxl)),
    elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = com.horsegallop.core.R.dimen.elevation_sm))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_md)),
      verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm)),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = stringResource(id = com.horsegallop.core.R.string.stats_title),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
      )
      Text(
        text = stringResource(id = com.horsegallop.core.R.string.stats_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm)),
        verticalAlignment = Alignment.CenterVertically
      ) {
        StatPill(
          modifier = Modifier.weight(1f),
          icon = { Icon(Icons.Filled.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
          value = String.format("%.1f", dailyDistance),
          label = stringResource(id = com.horsegallop.core.R.string.stats_today),
          progress = 0.5f,
          accent = MaterialTheme.colorScheme.primary
        )
        StatPill(
          modifier = Modifier.weight(1f),
          icon = { Icon(Icons.Filled.Explore, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
          value = String.format("%.1f", weeklyDistance),
          label = stringResource(id = com.horsegallop.core.R.string.stats_week),
          progress = 0.5f,
          accent = MaterialTheme.colorScheme.secondary
        )
        StatPill(
          modifier = Modifier.weight(1f),
          icon = { Icon(Icons.Filled.ShowChart, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
          value = String.format("%.1f", totalDistance),
          label = stringResource(id = com.horsegallop.core.R.string.stats_total),
          progress = 0.5f,
          accent = MaterialTheme.colorScheme.tertiary
        )
      }
    }
  }
}

@Composable
private fun StatPill(
  modifier: Modifier = Modifier,
  icon: @Composable () -> Unit,
  value: String,
  label: String,
  progress: Float,
  accent: Color
) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_xxl)),
    elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = com.horsegallop.core.R.dimen.elevation_sm)),
    border = BorderStroke(dimensionResource(id = com.horsegallop.core.R.dimen.width_divider_thin), accent.copy(alpha = 0.35f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_sm)),
      verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_xs)),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(dimensionResource(id = com.horsegallop.core.R.dimen.icon_lg))
          .clip(CircleShape)
          .background(accent.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) { icon() }
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      // subtle progress indicator
      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(50))
      ) {
        // track
        drawRoundRect(
          color = Color.Black.copy(alpha = 0.08f),
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(50f, 50f)
        )
        // progress
        val widthPx = size.width * progress.coerceIn(0f, 1f)
        drawRoundRect(
          color = accent,
          size = androidx.compose.ui.geometry.Size(widthPx, size.height),
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(50f, 50f)
        )
      }
    }
  }
}

@Composable
private fun RideTypeChip(
  type: RideType,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  OutlinedButton(
    onClick = onClick,
    modifier = modifier
      .heightIn(min = dimensionResource(id = com.horsegallop.core.R.dimen.height_button_lg)),
    shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
    colors = ButtonDefaults.outlinedButtonColors(
      containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
      contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    ),
    border = BorderStroke(dimensionResource(id = com.horsegallop.core.R.dimen.width_divider_thin), MaterialTheme.colorScheme.primary) // themed border
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_xs)),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(type.emoji, maxLines = 1)
        Text(
          localizedRideTypeName(type),
          style = MaterialTheme.typography.labelLarge,
          maxLines = 2
        )
      }
    }
  }
}

@Composable
private fun MiniStat(title: String, value: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = title,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_xs)))
    Text(
      text = value,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
private fun WeatherTopRow(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(0.dp)
  ) {
    Text(
      text = stringResource(id = com.horsegallop.core.R.string.ride_headline),
      style = MaterialTheme.typography.headlineSmall,
      color = MaterialTheme.colorScheme.primary,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)))
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm))
    ) {
      Box(
        modifier = Modifier
          .size(dimensionResource(id = com.horsegallop.core.R.dimen.icon_xl))
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Text("☀️")
      }
      Text(
        text = stringResource(id = com.horsegallop.core.R.string.ride_conditions_detail),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun localizedRideTypeName(type: RideType): String {
  return when (type) {
    RideType.DRESSAGE -> stringResource(id = com.horsegallop.core.R.string.ride_type_dressage)
    RideType.SHOW_JUMPING -> stringResource(id = com.horsegallop.core.R.string.ride_type_show_jumping)
    RideType.ENDURANCE -> stringResource(id = com.horsegallop.core.R.string.ride_type_endurance)
    RideType.TRAIL_RIDING -> stringResource(id = com.horsegallop.core.R.string.ride_type_trail_riding)
  }
}



@Composable
private fun StartRideHero(onStart: () -> Unit) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), 
    shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_xxl))
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_lg)),
      verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text("Track your next ride", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
      Text("See your route, speed and time in real-time.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f), textAlign = TextAlign.Center)
      Card(
        modifier = Modifier.clip(RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_xl))).clickable { onStart() }, 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Row(
          modifier = Modifier.padding(
            horizontal = dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_lg), 
            vertical = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)
          ), 
          verticalAlignment = Alignment.CenterVertically, 
          horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm))
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
          Text(stringResource(id = com.horsegallop.core.R.string.start_ride), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun RideMapWithTimer(path: List<GeoPoint>, elapsedSec: Int) {
  val context = LocalContext.current
  var isMyLocationEnabled by remember { mutableStateOf(false) }
  val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
    contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    isMyLocationEnabled = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true || 
                          permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
  }
  
  LaunchedEffect(Unit) {
      if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
          androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
          isMyLocationEnabled = true
      } else {
          permissionLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION))
      }
  }

  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), 
    shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_xxl))
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_sm)), 
      verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1.4f)
          .clip(RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)))
          .background(MaterialTheme.colorScheme.surfaceVariant)
      ) {
        // Filter out invalid (0,0) points which might come from emulator or bad GPS
        val validPath = remember(path) { path.filter { it.latitude != 0.0 || it.longitude != 0.0 } }
        
        // Default to Istanbul if path is empty, or last known point
        val defaultLocation = LatLng(41.0082, 28.9784)
        val initialPoint = validPath.lastOrNull()?.let { LatLng(it.latitude, it.longitude) } ?: defaultLocation
        
        val cameraPositionState = rememberCameraPositionState {
          position = CameraPosition.fromLatLngZoom(initialPoint, 15f)
        }
        
        // Auto-zoom logic - optimized to not freeze on every update
        LaunchedEffect(validPath) {
            if (validPath.size > 1) {
                val boundsBuilder = com.google.android.gms.maps.model.LatLngBounds.Builder()
                validPath.forEach { boundsBuilder.include(LatLng(it.latitude, it.longitude)) }
                try {
                    val bounds = boundsBuilder.build()
                    cameraPositionState.animate(com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(bounds, 100))
                } catch (e: Exception) {
                    // Handle single point or invalid bounds
                }
            } else if (validPath.isNotEmpty()) {
                val p = validPath.last()
                cameraPositionState.animate(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(LatLng(p.latitude, p.longitude), 15f))
            }
        }

        GoogleMap(
          modifier = Modifier.matchParentSize(),
          cameraPositionState = cameraPositionState,
          properties = MapProperties(isMyLocationEnabled = isMyLocationEnabled),
          uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = isMyLocationEnabled)
        ) {
          if (validPath.size >= 2) {
            val points = validPath.map { LatLng(it.latitude, it.longitude) }
            Polyline(
                points = points, 
                color = MaterialTheme.colorScheme.primary, 
                width = 12f,
                geodesic = true,
                jointType = com.google.android.gms.maps.model.JointType.ROUND
            )
          }
          val lastPoint = validPath.lastOrNull()
          if (lastPoint != null) {
            Marker(state = MarkerState(LatLng(lastPoint.latitude, lastPoint.longitude)))
          }
          val startPoint = validPath.firstOrNull()
          if (startPoint != null && startPoint != lastPoint) {
            Marker(
                state = MarkerState(LatLng(startPoint.latitude, startPoint.longitude)),
                icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN)
            )
          }
        }
        
        // Timer Overlay
        Box(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 16.dp)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
            .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
          val mm: Int = elapsedSec / 60
          val ss: Int = elapsedSec % 60
          Text(
            text = String.format("%02d:%02d", mm, ss),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
          )
        }
      }
    }
  }
}
@Composable
private fun StatsRow(speedKmh: Float, distanceKm: Float, durationSec: Int) {
  Row(
    modifier = Modifier.fillMaxWidth(), 
    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
  ) {
    MetricCard(title = stringResource(id = com.horsegallop.core.R.string.label_speed), value = String.format("%d", speedKmh.toInt()), unit = stringResource(id = com.horsegallop.core.R.string.unit_kmh), accent = MaterialTheme.colorScheme.primary)
    MetricCard(title = stringResource(id = com.horsegallop.core.R.string.label_distance), value = String.format("%.2f", distanceKm), unit = stringResource(id = com.horsegallop.core.R.string.unit_km), accent = MaterialTheme.colorScheme.secondary)
    val mm: Int = durationSec / 60
    val ss: Int = durationSec % 60
    MetricCard(title = stringResource(id = com.horsegallop.core.R.string.label_time), value = String.format("%02d:%02d", mm, ss), unit = "", accent = MaterialTheme.colorScheme.tertiary)
  }
}

@Composable
private fun MetricCard(title: String, value: String, unit: String, accent: Color) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), 
    shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_xl))
  ) {
    Column(
      modifier = Modifier.padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_sm)), 
      horizontalAlignment = Alignment.Start, 
      verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_xs))
    ) {
      Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Row(
        verticalAlignment = Alignment.Bottom, 
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm_half))
      ) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        if (unit.isNotEmpty()) Text(text = unit, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Canvas(modifier = Modifier.fillMaxWidth().height(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_xs))) { 
        drawLine(
          color = accent, 
          start = Offset(0f, size.height / 2), 
          end = Offset(size.width, size.height / 2), 
          strokeWidth = 8f, 
          cap = StrokeCap.Round
        ) 
      }
    }
  }
}

@Composable
private fun CaloriesCard(calories: Int) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), 
    shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_xl))
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_md)), 
      horizontalArrangement = Arrangement.SpaceBetween, 
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_xs))) {
        Text(stringResource(id = com.horsegallop.core.R.string.calories_burned), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("$calories kcal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
      }
      // decorative marker
      Box(
        modifier = Modifier
          .size(dimensionResource(id = com.horsegallop.core.R.dimen.icon_xs))
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.secondary)
      )
    }
  }
}

@Composable
private fun TrendsSection(daily: List<Float>, weekly: List<Float>) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), 
    shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_xl))
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_md)), 
      verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
    ) {
      Text(stringResource(id = com.horsegallop.core.R.string.daily_ride_trend), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
      SimpleBars(values = daily, barColor = MaterialTheme.colorScheme.primary)
      HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
      Text(stringResource(id = com.horsegallop.core.R.string.weekly_progress), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
      SimpleBars(values = weekly, barColor = MaterialTheme.colorScheme.secondary)
    }
  }
}

@Composable
private fun SimpleBars(values: List<Float>, barColor: Color) {
  val maxVal: Float = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)
  val barSpacing: Float = 8f
  val barCorner: Float = 12f
  
  Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
    val count: Int = values.size
    if (count == 0) return@Canvas
    val totalSpacing: Float = barSpacing * (count - 1)
    val barWidth: Float = (size.width - totalSpacing) / count
    
    values.forEachIndexed { index: Int, v: Float ->
      val frac: Float = (v / maxVal).coerceIn(0f, 1f)
      val barHeight: Float = size.height * frac
      val left: Float = index * (barWidth + barSpacing)
      val top: Float = size.height - barHeight
      
      drawRoundRect(
        color = barColor.copy(alpha = 0.85f),
        topLeft = Offset(left, top),
        size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(barCorner, barCorner)
      )
    }
  }
}

@Composable
private fun AchievementCard(text: String) {
  var visible: Boolean by remember { mutableStateOf(true) }
  AnimatedVisibility(visible = visible, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), 
      shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_xl))
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_md)), 
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)), 
        verticalAlignment = Alignment.CenterVertically
      ) {
        // decorative icon placeholder
        Box(
          modifier = Modifier
            .size(dimensionResource(id = com.horsegallop.core.R.dimen.icon_xs))
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.tertiary)
        )
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
}

@Composable
private fun ControlsRow(isRiding: Boolean, onStop: () -> Unit, autoDetect: Boolean, onToggleAuto: (Boolean) -> Unit) {
  var showStopDialog by remember { mutableStateOf(false) }

  if (showStopDialog) {
      AlertDialog(
          onDismissRequest = { showStopDialog = false },
          title = { Text(stringResource(id = com.horsegallop.core.R.string.stop_ride_confirmation_title)) },
          text = { Text(stringResource(id = com.horsegallop.core.R.string.stop_ride_confirmation_message)) },
          confirmButton = {
              TextButton(
                  onClick = {
                      showStopDialog = false
                      onStop()
                  }
              ) {
                  Text(stringResource(id = com.horsegallop.core.R.string.action_confirm_stop))
              }
          },
          dismissButton = {
              TextButton(onClick = { showStopDialog = false }) {
                  Text(stringResource(id = com.horsegallop.core.R.string.action_cancel))
              }
          }
      )
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_xxl))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_md)),
      horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm)),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Card(
        modifier = Modifier.weight(1f).clickable { 
            if (isRiding) {
                showStopDialog = true
            } else {
                onStop()
            }
        }, 
        colors = CardDefaults.cardColors(containerColor = if (isRiding) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary), 
        shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_sm)),
          horizontalArrangement = Arrangement.Center, 
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            if (isRiding) Icons.Default.Stop else Icons.Default.PlayArrow, 
            contentDescription = null, 
            tint = if (isRiding) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
          )
          Spacer(Modifier.width(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm)))
          Text(
            if (isRiding) stringResource(id = com.horsegallop.core.R.string.finish_ride) else stringResource(id = com.horsegallop.core.R.string.start_ride),
            color = if (isRiding) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
          )
        }
      }
      
      Card(
        modifier = Modifier.width(120.dp),
        colors = CardDefaults.cardColors(containerColor = if (autoDetect) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
        border = androidx.compose.foundation.BorderStroke(
          dimensionResource(id = com.horsegallop.core.R.dimen.width_divider_thin), 
          if (autoDetect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_sm)),
          horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm)),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(stringResource(id = com.horsegallop.core.R.string.auto_ride_detection), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
          Switch(
            checked = autoDetect, 
            onCheckedChange = onToggleAuto, 
            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
          )
        }
      }
    }
  }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun BarnSelection(
    barns: List<BarnWithLocation>,
    selectedBarn: BarnWithLocation?,
    onBarnSelected: (BarnWithLocation) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_xxl)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_md)),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id = com.horsegallop.core.R.string.select_barn),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedBarn?.barn?.name ?: stringResource(id = com.horsegallop.core.R.string.select_barn_hint),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    barns.forEach { barn ->
                        DropdownMenuItem(
                            text = { Text(barn.barn.name) },
                            onClick = {
                                onBarnSelected(barn)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
@androidx.compose.material3.ExperimentalMaterial3Api
private fun PreviewRideTracking() {
  RideTrackingContent(
    state = RideUiState(
        speedKmh = 0f,
        distanceKm = 0f,
        durationSec = 0,
        calories = 0,
        isRiding = false,
        autoDetect = false,
        dailyTrend = listOf(0.2f, 0.4f, 0.1f, 0.6f, 0.3f, 0.7f, 0.5f),
        weeklyTrend = listOf(1.2f, 2.4f, 3.1f, 2.7f),
        pathPoints = listOf(GeoPoint(41.0, 29.0))
    ),
    onToggleRide = {},
    onSetAutoDetect = {},
    onBarnSelected = {}
  )
}
