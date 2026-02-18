@file:OptIn(ExperimentalMaterial3Api::class)
package com.horsegallop.feature.horse.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import coil.compose.AsyncImage
import com.horsegallop.domain.horse.model.Horse

@Composable
fun HorseDashboardScreen(
    onBack: () -> Unit,
   horse: Horse?
) {
    // Default to mock data if null
    val currentHorse = horse ?: Horse(
        name = "Stormbreaker",
        breed = "Arabian",
        age = 7,
        photoUrl = "https://images.unsplash.com/photo-1553284965-83fd3e82fa5a?auto=format&fit=crop&w=400&q=80",
        lastFarrierDate = System.currentTimeMillis() - (35 * 24 * 60 * 60 * 1000L), // 35 days ago
        lastVetCheckDate = System.currentTimeMillis() - (120 * 24 * 60 * 60 * 1000L), // 120 days ago
        ownerId = "user_123"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Horse") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Horse Hero Card
            item { HorseHeroCard(horse = currentHorse) }

            // Health Insights (Smart Reminders)
            item { HealthAlertSection(horse = currentHorse) }

            // Barn Info Card
            item { BarnInfoCard(horse = currentHorse) }

            // upcoming Appointments Card
            item { UpcomingAppointmentsCard(horse = currentHorse) }

            // Quick Actions Grid
            item { QuickActionGrid() }
        }
    }
}

@Composable
fun HorseHeroCard(horse: Horse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = horse.photoUrl,
                contentDescription = "${horse.name} photo",
                modifier = Modifier.size(100.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(horse.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    // Badge for age
                    Badge(
                        modifier = Modifier.padding(start = 8.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Text("${horse.age} yrs")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                // Breed info
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Pets, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${horse.breed}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Health Status Badge
                val healthStatus = getHealthStatus(horse)
                Card(
                    modifier = Modifier.padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = getHealthColor(healthStatus))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            when (healthStatus) {
                                HealthStatus.HEALTHY -> Icons.Default.CheckCircle
                                HealthStatus.NEEDS_ATTENTION -> Icons.Default.Warning
                                HealthStatus.URGENT -> Icons.Default.Error
                            },
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Text(
                            when (healthStatus) {
                                HealthStatus.HEALTHY -> "HEALTHY"
                                HealthStatus.NEEDS_ATTENTION -> "Needs Attention"
                                HealthStatus.URGENT -> "Urgent!"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

enum class HealthStatus {
    HEALTHY,
    NEEDS_ATTENTION,
    URGENT
}

@Composable
fun getHealthStatus(horse: Horse): HealthStatus {
    val daysSinceFarrier = getDaysSince(horse.lastFarrierDate)
    val daysSinceVet = getDaysSince(horse.lastVetCheckDate)
    
    // Standard farrier cycle: 6 weeks (42 days)
    // Standard vet check: every 4 months (~120 days)
    
    if (daysSinceFarrier > 56 || daysSinceVet > 180) {
        return HealthStatus.URGENT
    }
    
    if (daysSinceFarrier > 49 || daysSinceVet > 150) {
        return HealthStatus.NEEDS_ATTENTION
    }
    
    return HealthStatus.HEALTHY
}

@Composable
fun getHealthColor(status: HealthStatus): Color {
    return when (status) {
        HealthStatus.HEALTHY -> Color(0xFF4CAF50)
        HealthStatus.NEEDS_ATTENTION -> Color(0xFFFF9800)
        HealthStatus.URGENT -> Color(0xFFF44336)
    }
}

fun getDaysSince(date: Long): Int {
    if (date == 0L) return Int.MAX_VALUE
    return ((System.currentTimeMillis() - date) / (24 * 60 * 60 * 1000L)).toInt()
}

@Composable
fun HealthAlertSection(horse: Horse) {
    val daysSinceFarrier = getDaysSince(horse.lastFarrierDate)
    val daysSinceVet = getDaysSince(horse.lastVetCheckDate)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Health Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Farrier Item
            HealthItem(
                icon = Icons.Default.Handyman,
                label = "Last Farrier Visit",
                value = if (daysSinceFarrier > 365) "Never" else "$daysSinceFarrier days ago",
                nextDue = "${42 - daysSinceFarrier} days until next",
                isUrgent = daysSinceFarrier > 49,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Vet Item
            HealthItem(
                icon = Icons.Default.MedicalServices,
                label = "Last Vet Check",
                value = if (daysSinceVet > 365) "Never" else "$daysSinceVet days ago",
                nextDue = "${120 - daysSinceVet} days until next",
                isUrgent = daysSinceVet > 150,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Weight Item
            HealthItem(
                icon = Icons.Default.Scale,
                label = "Weight",
                value = if (horse.weight > 0) "${horse.weight} kg" else "Not tracked",
                nextDue = "-",
                isUrgent = false
            )
        }
    }
}

@Composable
fun HealthItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, nextDue: String, isUrgent: Boolean, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isUrgent) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (isUrgent) {
                    Icon(Icons.Default.Warning, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
            Text(nextDue, style = MaterialTheme.typography.labelSmall, color = if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun BarnInfoCard(horse: Horse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Stable Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            InfoRow(label = "Barn Name", value = "Downtown Equestrian Center")
            InfoRow(label = "Address", value = "123 Horse Road, City Center")
            InfoRow(label = "Contact", value = "+1 (555) 123-4567")
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { /* Navigate to barn details */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Barn Details")
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("$label:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(8.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun UpcomingAppointmentsCard(horse: Horse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Upcoming", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = { /* Show all appointments */ }) {
                    Icon(Icons.Default.CalendarMonth, null)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mock upcoming appointment
            AppointmentItem(
                icon = Icons.Default.Handyman,
                title = "Farrier Visit",
                subtitle = "Regular hoof trimming",
                date = "Feb 28, 2026",
                time = "10:00 AM",
                status = "Upcoming"
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            AppointmentItem(
                icon = Icons.Default.MedicalServices,
                title = "Vet Checkup",
                subtitle = "Quarterly health check",
                date = "Mar 15, 2026",
                time = "02:30 PM",
                status = "Upcoming"
            )
        }
    }
}

@Composable
fun AppointmentItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, date: String, time: String, status: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                if (status == "Upcoming") {
                    Card(
                        modifier = Modifier.padding(start = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Text(status, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(date, style = MaterialTheme.typography.labelSmall)
            Text(time, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun QuickActionGrid() {
    Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(12.dp))
    
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ActionItem(
            modifier = Modifier.weight(1f),
            title = "Add Vet Log",
            icon = Icons.Default.MedicalServices,
            color = Color(0xFF2196F3),
            onClick = { /* Add vet log */ }
        )
        ActionItem(
            modifier = Modifier.weight(1f),
            title = "Add Training",
            icon = Icons.Default.SportsScore,
            color = Color(0xFFFF9800),
            onClick = { /* Add training log */ }
        )
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ActionItem(
            modifier = Modifier.weight(1f),
            title = "Add Weight",
            icon = Icons.Default.Scale,
            color = Color(0xFF9C27B0),
            onClick = { /* Add weight */ }
        )
        ActionItem(
            modifier = Modifier.weight(1f),
            title = "Notes",
            icon = Icons.Default.Note,
            color = Color(0xFF607D8B),
            onClick = { /* Add notes */ }
        )
    }
}

@Composable
fun ActionItem(modifier: Modifier, title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        onClick = onClick
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
