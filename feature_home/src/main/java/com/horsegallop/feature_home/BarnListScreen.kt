package com.horsegallop.feature_home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class BarnUi(val id: String, val name: String, val description: String)

@Composable
fun BarnListScreen(onBarnClick: (BarnUi) -> Unit) {
    val demo: List<BarnUi> = listOf(
        BarnUi("1", "Adin Country", "Beginner to Pro rides"),
        BarnUi("2", "Sable Ranch", "Trail and endurance"),
        BarnUi("3", "Silver Hoof", "Dressage & Jumping")
    )
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(12.dp)) {
        items(demo) { barn ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBarnClick(barn) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(barn.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(barn.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}


