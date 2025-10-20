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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
  import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
    viewModelScope.launch {
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
  private fun stopRide() { _uiState.value = _uiState.value.copy(speedKmh = 0f) }
}

@Composable
@androidx.compose.material3.ExperimentalMaterial3Api
fun RideTrackingScreen(viewModel: RideTrackingViewModel) {
  val state: RideUiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current
  // Location tracking service removed - using mock data for now
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(if (state.isRiding) "Riding..." else "Ready to Ride?", fontWeight = FontWeight.Bold) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      WelcomeHeader()
      if (!state.isRiding) {
        StartRideHero(onStart = { viewModel.toggleRide() })
      } else {
        RideMapWithTimer(
          path = state.pathPoints,
          elapsedSec = state.durationSec
        )
        Spacer(Modifier.height(8.dp))
        StatsRow(speedKmh = state.speedKmh, distanceKm = state.distanceKm, durationSec = state.durationSec)
        ControlsRow(isRiding = state.isRiding, onStop = { viewModel.toggleRide() }, autoDetect = state.autoDetect, onToggleAuto = { viewModel.setAutoDetect(it) })
      }
    }
  }
}

@Composable
private fun WelcomeHeader() {
    val fade by animateFloatAsState(targetValue = 1f, animationSpec = tween(600), label = "fade")
    Text("🐴 Hello, Rider!", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
  HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}

@Composable
private fun StartRideHero(onStart: () -> Unit) {
  Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(24.dp)) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text("Track your next ride", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
      Text("See your route, speed and time in real-time.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f), textAlign = TextAlign.Center)
      Card(modifier = Modifier.clip(RoundedCornerShape(18.dp)).clickable { onStart() }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
        Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
          Text("Start Ride", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun RideMapWithTimer(path: List<GeoPoint>, elapsedSec: Int) {
  Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(24.dp)) {
    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      // Map-like canvas
      Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.4f).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
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
        Row(modifier = Modifier.align(Alignment.TopStart).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(Modifier.size(4.dp))
          Text("Live Route", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
      // Timer
      val mm: Int = elapsedSec / 60
      val ss: Int = elapsedSec % 60
      Text(text = String.format("%02d:%02d", mm, ss), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
  }
}
@Composable
private fun StatsRow(speedKmh: Float, distanceKm: Float, durationSec: Int) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    MetricCard(title = "Speed", value = String.format("%d", speedKmh.toInt()), unit = "km/h", accent = MaterialTheme.colorScheme.primary)
    MetricCard(title = "Distance", value = String.format("%.2f", distanceKm), unit = "km", accent = MaterialTheme.colorScheme.secondary)
    val mm: Int = durationSec / 60
    val ss: Int = durationSec % 60
    MetricCard(title = "Time", value = String.format("%02d:%02d", mm, ss), unit = "", accent = MaterialTheme.colorScheme.tertiary)
  }
}

@Composable
private fun MetricCard(title: String, value: String, unit: String, accent: Color) {
  Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp)) {
    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        if (unit.isNotEmpty()) Text(text = unit, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Canvas(modifier = Modifier.fillMaxWidth().height(4.dp)) { drawLine(color = accent, start = Offset(0f, size.height / 2), end = Offset(size.width, size.height / 2), strokeWidth = 8f, cap = StrokeCap.Round) }
    }
  }
}

@Composable
private fun CaloriesCard(calories: Int) {
  Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp)) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Calories Burned", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("$calories kcal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
      }
      // decorative marker
      Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary))
    }
  }
}

@Composable
private fun TrendsSection(daily: List<Float>, weekly: List<Float>) {
  Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp)) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text("Daily Ride Trend", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      SimpleBars(values = daily, barColor = MaterialTheme.colorScheme.primary)
      HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
      Text("Weekly Progress", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(20.dp)) {
      Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        // decorative icon placeholder
        Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
      }
    }
  }
}

@Composable
private fun ControlsRow(isRiding: Boolean, onStop: () -> Unit, autoDetect: Boolean, onToggleAuto: (Boolean) -> Unit) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
    Card(modifier = Modifier.weight(1f).clickable { if (isRiding) onStop() }, colors = CardDefaults.cardColors(containerColor = if (isRiding) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(20.dp)) {
      Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        if (!isRiding) Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
        Spacer(Modifier.size(8.dp))
        Text(if (isRiding) "Stop Ride" else "Start Ride", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
      }
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp)) {
      Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Auto Ride Detection", style = MaterialTheme.typography.bodySmall)
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


