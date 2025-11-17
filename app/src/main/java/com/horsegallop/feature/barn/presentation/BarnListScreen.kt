package com.horsegallop.feature.barn.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.horsegallop.feature.barn.domain.model.BarnUi

@Composable
fun BarnListScreen(
  onBarnClick: (BarnUi) -> Unit,
  onHomeClick: () -> Unit = {},
  onRideClick: () -> Unit = {}
) {
  data class BarnWithLocation(val barn: BarnUi, val lat: Double, val lng: Double)
  val demo: List<BarnWithLocation> = listOf(
    BarnWithLocation(BarnUi("1", "Adin Country", "Beginner to Pro rides"), 41.0082, 28.9784),
    BarnWithLocation(BarnUi("2", "Sable Ranch", "Trail and endurance"), 41.0151, 29.0037),
    BarnWithLocation(BarnUi("3", "Silver Hoof", "Dressage & Jumping"), 41.0258, 29.0150)
  )
  var query: String by remember { mutableStateOf("") }
  val filtered: List<BarnWithLocation> = remember(query, demo) {
    if (query.isBlank()) demo else demo.filter { item ->
      item.barn.name.contains(query, ignoreCase = true) || item.barn.description.contains(query, ignoreCase = true)
    }
  }
  Scaffold(
    bottomBar = {
      NavigationBar {
        NavigationBarItem(
          icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
          label = { Text("Home") },
          selected = false,
          onClick = onHomeClick
        )
        NavigationBarItem(
          icon = { Icon(Icons.Filled.DirectionsRun, contentDescription = "Ride") },
          label = { Text("Ride") },
          selected = false,
          onClick = onRideClick
        )
        NavigationBarItem(
          icon = { Icon(Icons.Filled.List, contentDescription = "Barns") },
          label = { Text("Barns") },
          selected = true,
          onClick = { /* Already on barns */ }
        )
      }
    }
  ) { padding ->
    Column(modifier = Modifier.padding(padding).padding(12.dp)) {
      OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        placeholder = { Text(text = "Çiftlik ara (isim veya açıklama)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
      )
      Spacer(modifier = Modifier.height(12.dp))
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().aspectRatio(1.4f)
      ) {
        Box(
          modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
          contentAlignment = Alignment.TopStart
        ) {
          val primaryColor = MaterialTheme.colorScheme.primary
          val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
          val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.06f)
          Canvas(modifier = Modifier.fillMaxSize()) {
            // Subtle grid
            val step = 40.dp.toPx()
            var gx = 0f
            while (gx < size.width) {
              drawLine(gridColor, Offset(gx, 0f), Offset(gx, size.height), 1f)
              gx += step
            }
            var gy = 0f
            while (gy < size.height) {
              drawLine(gridColor, Offset(0f, gy), Offset(size.width, gy), 1f)
              gy += step
            }
            val minLat = filtered.minOfOrNull { it.lat } ?: 0.0
            val maxLat = filtered.maxOfOrNull { it.lat } ?: 1.0
            val minLng = filtered.minOfOrNull { it.lng } ?: 0.0
            val maxLng = filtered.maxOfOrNull { it.lng } ?: 1.0
            val w = size.width
            val h = size.height
            filtered.forEach { item ->
              val nx = if (maxLng != minLng) ((item.lng - minLng) / (maxLng - minLng)).toFloat() else 0.5f
              val ny = if (maxLat != minLat) (1f - ((item.lat - minLat) / (maxLat - minLat)).toFloat()) else 0.5f
              val x = nx * w
              val y = ny * h
              // Tail
              drawLine(color = primaryColor, start = Offset(x, y - 8.dp.toPx()), end = Offset(x, y + 10.dp.toPx()), strokeWidth = 4f)
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
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Yakındaki çiftlikler",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.weight(1f)
            )
            Row {
              IconButton(onClick = { /* zoom out placeholder */ }) {
                Icon(Icons.Filled.Remove, contentDescription = "Zoom out", tint = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              IconButton(onClick = { /* zoom in placeholder */ }) {
                Icon(Icons.Filled.Add, contentDescription = "Zoom in", tint = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
          Text(
            text = if (query.isBlank()) "Sonuç: ${filtered.size}" else "Arama: ${filtered.size} sonuç",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
          )
        }
      }
    }
  }
}


