@file:OptIn(ExperimentalMaterial3Api::class)
package com.horsegallop.feature.horse.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.horsegallop.domain.horse.model.Horse
import com.horsegallop.domain.horse.model.RecordType

@Composable
fun HorseDashboardScreen() {
    // Mock data for PO demo - Will be wired to ViewModel later
    val myHorse = Horse(
        name = "Stormbreaker",
        breed = "Arabian",
        age = 7,
        photoUrl = "https://images.unsplash.com/photo-1553284965-83fd3e82fa5a?auto=format&fit=crop&w=400&q=80",
        lastFarrierDate = System.currentTimeMillis() - (35 * 24 * 60 * 60 * 1000L) // 35 days ago
    )

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("My Digital Stable", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.NotificationsActive, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Horse Hero Card
            item {
                HorseHeroCard(horse = myHorse)
            }

            // Health Insights (Smart Reminders)
            item {
                HealthAlertSection(lastFarrier = myHorse.lastFarrierDate)
            }

            item {
                Text("Recent Activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            // Quick Actions Grid
            item {
                QuickActionGrid()
            }
        }
    }
}

@Composable
fun HorseHeroCard(horse: Horse) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = horse.photoUrl,
                contentDescription = null,
                modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.Gray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(horse.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text("${horse.breed} • ${horse.age} Years", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        "HEALTHY",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun HealthAlertSection(lastFarrier: Long) {
    val daysSinceFarrier = ((System.currentTimeMillis() - lastFarrier) / (24 * 60 * 60 * 1000L)).toInt()
    val needsFarrier = daysSinceFarrier > 40 // Standard 6-week cycle is ~42 days

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (needsFarrier) MaterialTheme.colorScheme.errorContainer else Color(0xFFE8F5E9)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (needsFarrier) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (needsFarrier) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    if (needsFarrier) "Farrier Needed Soon" else "Horse is on Schedule",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Last farrier visit was $daysSinceFarrier days ago.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun QuickActionGrid() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        ActionItem(Modifier.weight(1f), "Add Vet Log", Icons.Default.MedicalServices, Color(0xFF2196F3))
        ActionItem(Modifier.weight(1f), "Add Training", Icons.Default.SportsScore, Color(0xFFFF9800))
    }
}

@Composable
fun ActionItem(modifier: Modifier, title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.1f),
        onClick = { }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
