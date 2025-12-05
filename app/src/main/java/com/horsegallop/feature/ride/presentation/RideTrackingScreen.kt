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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.ShowChart
 
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.FieldValue

data class RideUiState(
  val speedKmh: Float,
  val distanceKm: Float,
  val durationSec: Int,
  val calories: Int,
  val isRiding: Boolean,
  val autoDetect: Boolean,
  val nextGoalText: String,
  val dailyTrend: List<Float>,
  val weeklyTrend: List<Float>,
  val pathPoints: List<GeoPoint>
)

data class GeoPoint(val latitude: Double, val longitude: Double)

class RideTrackingViewModel : ViewModel() {
  private val _uiState: MutableStateFlow<RideUiState> = MutableStateFlow(
    RideUiState(
      speedKmh = 0f,
      distanceKm = 0f,
      durationSec = 0,
      calories = 0,
      isRiding = false,
      autoDetect = false,
      nextGoalText = "Next Goal: 100 km Club",
      dailyTrend = listOf(0.2f, 0.4f, 0.1f, 0.6f, 0.3f, 0.7f, 0.5f),
      weeklyTrend = listOf(1.2f, 2.4f, 3.1f, 2.7f),
      pathPoints = listOf(GeoPoint(41.0, 29.0))
    )
  )
  val uiState: StateFlow<RideUiState> = _uiState
  fun toggleRide() {
    val now: RideUiState = _uiState.value
    _uiState.value = now.copy(isRiding = !now.isRiding)
    if (_uiState.value.isRiding) startMockLoop() else stopRide()
  }
  fun setAutoDetect(enabled: Boolean) { _uiState.value = _uiState.value.copy(autoDetect = enabled) }
  private fun startMockLoop() {
    viewModelScope.launch(Dispatchers.Default) {
      while (_uiState.value.isRiding) {
        delay(1000)
        val cur: RideUiState = _uiState.value
        val newDuration: Int = cur.durationSec + 1
        val newSpeed: Float = ((10..22).random()) / 2f
        val newDistance: Float = cur.distanceKm + (newSpeed / 3600f)
        val weightKg: Float = 75f
        val met: Float = 5.5f
        val kcal: Int = (weightKg * (newDuration / 60f) * met / 60f).toInt()
        // advance mock position slightly
        val last: GeoPoint = cur.pathPoints.lastOrNull() ?: GeoPoint(41.0, 29.0)
        val jitterLat: Double = (listOf(-0.0005, -0.0003, 0.0, 0.0003, 0.0005)).random()
        val jitterLng: Double = (listOf(-0.0005, -0.0003, 0.0, 0.0003, 0.0005)).random()
        val next: GeoPoint = GeoPoint(last.latitude + jitterLat, last.longitude + jitterLng)
        val updatedPath: List<GeoPoint> = (cur.pathPoints + next).takeLast(200)
        _uiState.value = cur.copy(
          speedKmh = newSpeed,
          distanceKm = newDistance,
          durationSec = newDuration,
          calories = kcal,
          pathPoints = updatedPath
        )
      }
    }
  }
  private fun saveRideToFirestore() {
    val cur: RideUiState = _uiState.value
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val uid: String? = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
          val data: MutableMap<String, Any> = hashMapOf(
            "title" to "Ride",
            "durationMin" to (cur.durationSec / 60),
            "distanceKm" to cur.distanceKm.toDouble(),
            "timestamp" to FieldValue.serverTimestamp()
          )
          Firebase.firestore.collection("users").document(uid).collection("rides").add(data)
        }
      } catch (_: Throwable) { }
    }
  }
  private fun stopRide() {
    val cur: RideUiState = _uiState.value
    _uiState.value = cur.copy(speedKmh = 0f, isRiding = false)
    saveRideToFirestore()
  }
}

@Composable
@androidx.compose.material3.ExperimentalMaterial3Api
fun RideTrackingScreen(
  viewModel: RideTrackingViewModel,
  onHomeClick: () -> Unit = {},
  onBarnsClick: () -> Unit = {}
) {
  val state: RideUiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current
  var selectedRideType: RideType? by remember { mutableStateOf<RideType?>(null) }
  // Location tracking service removed - using mock data for now
  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = { /* No title - greeting moved into content */ }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        
        .padding(horizontal = dimensionResource(id = com.horsegallop.core.R.dimen.padding_screen_horizontal))
        ,
      verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm))
    ) {
      WelcomeHeader()
      WeatherTopRow()
      if (!state.isRiding) {
        RideTypeCard(
          selectedRideType = selectedRideType,
          onRideTypeSelected = { selectedRideType = it }
        )
        Spacer(Modifier.height(dimensionResource(id = com.horsegallop.core.R.dimen.section_spacing_md)))
        StatsOverviewCard()
        Spacer(modifier = Modifier.weight(1f))
        Button(
          onClick = { viewModel.toggleRide() },
          modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = com.horsegallop.core.R.dimen.height_button_xl))
            ,
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = dimensionResource(id = com.horsegallop.core.R.dimen.elevation_sm))
        ) {
          Text(
            text = stringResource(id = com.horsegallop.core.R.string.start_ride),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
          )
        }
      } else {
        RideMapWithTimer(
          path = state.pathPoints,
          elapsedSec = state.durationSec
        )
        Spacer(Modifier.height(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)))
      StatsRow(speedKmh = state.speedKmh, distanceKm = state.distanceKm, durationSec = state.durationSec)
        Spacer(Modifier.height(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm)))
        ControlsRow(isRiding = state.isRiding, onStop = { viewModel.toggleRide() }, autoDetect = state.autoDetect, onToggleAuto = { viewModel.setAutoDetect(it) })
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
private fun StatsOverviewCard() {
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
          value = "0",
          label = stringResource(id = com.horsegallop.core.R.string.stats_today),
          progress = 0f,
          accent = MaterialTheme.colorScheme.primary
        )
        StatPill(
          modifier = Modifier.weight(1f),
          icon = { Icon(Icons.Filled.Explore, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
          value = "0",
          label = stringResource(id = com.horsegallop.core.R.string.stats_week),
          progress = 0f,
          accent = MaterialTheme.colorScheme.secondary
        )
        StatPill(
          modifier = Modifier.weight(1f),
          icon = { Icon(Icons.Filled.ShowChart, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
          value = "0",
          label = stringResource(id = com.horsegallop.core.R.string.stats_total),
          progress = 0f,
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
private fun WeatherTopRow() {
  Column(
    modifier = Modifier.fillMaxWidth(),
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
private fun WelcomeHeader() {
  val fade by animateFloatAsState(targetValue = 1f, animationSpec = tween(600), label = "fade")
  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    HorizontalDivider(
      color = MaterialTheme.colorScheme.surfaceVariant,
      modifier = Modifier.padding(
        horizontal = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_lg),
        vertical = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_xs)
      )
    )
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
          Text("Start Ride", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun RideMapWithTimer(path: List<GeoPoint>, elapsedSec: Int) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), 
    shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_xxl))
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_sm)), 
      verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
    ) {
      // Map-like canvas
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1.4f)
          .clip(RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)))
          .background(MaterialTheme.colorScheme.surfaceVariant)
      ) {
        // capture colors outside draw scope
        val primaryColor = MaterialTheme.colorScheme.primary
        val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
        Canvas(modifier = Modifier.matchParentSize()) {
          // draw subtle grid
          val step = 40f
          for (x in 0..(size.width / step).toInt()) {
            drawLine(color = Color.Black.copy(alpha = 0.06f), start = Offset(x * step, 0f), end = Offset(x * step, size.height), strokeWidth = 1f)
          }
          for (y in 0..(size.height / step).toInt()) {
            drawLine(color = Color.Black.copy(alpha = 0.06f), start = Offset(0f, y * step), end = Offset(size.width, y * step), strokeWidth = 1f)
          }
          // normalize geo points to canvas bounds
          val xs = path.map { it.longitude }
          val ys = path.map { it.latitude }
          if (xs.isNotEmpty() && ys.isNotEmpty()) {
            val minX = xs.minOrNull() ?: 0.0
            val maxX = xs.maxOrNull() ?: 1.0
            val minY = ys.minOrNull() ?: 0.0
            val maxY = ys.maxOrNull() ?: 1.0
            val pad = 24f
            var lastPoint: Offset? = null
            path.forEachIndexed { idx, p ->
              val nx = if (maxX == minX) 0.5f else ((p.longitude - minX) / (maxX - minX)).toFloat()
              val ny = if (maxY == minY) 0.5f else ((p.latitude - minY) / (maxY - minY)).toFloat()
              val x = pad + nx * (size.width - 2 * pad)
              val y = pad + (1f - ny) * (size.height - 2 * pad)
              val current = Offset(x, y)
              if (lastPoint != null) {
                drawLine(color = primaryColor, start = lastPoint!!, end = current, strokeWidth = 6f, cap = StrokeCap.Round)
              }
              lastPoint = current
              if (idx == path.lastIndex) {
                // current position marker
                drawCircle(color = primaryColor, radius = 10f, center = current)
                drawCircle(color = onPrimaryColor, radius = 4f, center = current)
              }
            }
            // start flag
            val start = path.first()
            val sx = if (maxX == minX) 0.5f else ((start.longitude - minX) / (maxX - minX)).toFloat()
            val sy = if (maxY == minY) 0.5f else ((start.latitude - minY) / (maxY - minY)).toFloat()
            val sPt = Offset(pad + sx * (size.width - 2 * pad), pad + (1f - sy) * (size.height - 2 * pad))
            drawCircle(color = Color.Black.copy(alpha = 0.15f), radius = 8f, center = sPt)
          }
          // border
          drawRoundRect(color = Color.Black.copy(alpha = 0.08f), style = Stroke(width = 2f))
        }
        // decorative corner label
        Row(
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm)), 
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(Modifier.size(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_xs)))
          Text("Live Route", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
      // Timer
      val mm: Int = elapsedSec / 60
      val ss: Int = elapsedSec % 60
      Text(
        text = String.format("%02d:%02d", mm, ss), 
        style = MaterialTheme.typography.headlineSmall, 
        fontWeight = FontWeight.Bold, 
        modifier = Modifier.align(Alignment.CenterHorizontally)
      )
    }
  }
}
@Composable
private fun StatsRow(speedKmh: Float, distanceKm: Float, durationSec: Int) {
  Row(
    modifier = Modifier.fillMaxWidth(), 
    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
  ) {
    MetricCard(title = "Speed", value = String.format("%d", speedKmh.toInt()), unit = "km/h", accent = MaterialTheme.colorScheme.primary)
    MetricCard(title = "Distance", value = String.format("%.2f", distanceKm), unit = "km", accent = MaterialTheme.colorScheme.secondary)
    val mm: Int = durationSec / 60
    val ss: Int = durationSec % 60
    MetricCard(title = "Time", value = String.format("%02d:%02d", mm, ss), unit = "", accent = MaterialTheme.colorScheme.tertiary)
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
        Text("Calories Burned", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
      Text("Daily Ride Trend", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
      SimpleBars(values = daily, barColor = MaterialTheme.colorScheme.primary)
      HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
      Text("Weekly Progress", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
  Row(
    modifier = Modifier.fillMaxWidth(), 
    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)), 
    verticalAlignment = Alignment.CenterVertically
  ) {
    Card(
      modifier = Modifier.weight(1f).clickable { if (isRiding) onStop() }, 
      colors = CardDefaults.cardColors(containerColor = if (isRiding) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary), 
      shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_xl))
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)), 
        horizontalArrangement = Arrangement.Center, 
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (!isRiding) Icon(Icons.Default.PlayArrow, contentDescription = null, tint = if (isRiding) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary)
        Spacer(Modifier.size(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm)))
        Text(if (isRiding) "Stop Ride" else "Start Ride", color = if (isRiding) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
      }
    }
    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), 
      shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_xl))
    ) {
      Row(
        modifier = Modifier.padding(
          horizontal = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md), 
          vertical = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm)
        ), 
        verticalAlignment = Alignment.CenterVertically, 
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm))
      ) {
        Text("Auto Ride Detection", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        Switch(checked = autoDetect, onCheckedChange = onToggleAuto, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary))
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRideTracking() {
  RideTrackingScreen(viewModel = RideTrackingViewModel())
}
