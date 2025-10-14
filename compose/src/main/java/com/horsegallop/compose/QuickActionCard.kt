package com.horsegallop.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon

@Composable
fun QuickActionCard(
  title: String,
  icon: ImageVector,
  color: Color,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .width(140.dp)
      .height(140.dp),
    onClick = onClick,
    colors = CardDefaults.cardColors(
      containerColor = color.copy(alpha = 0.1f)
    ),
    shape = RoundedCornerShape(16.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(32.dp)
          )
        }
        Text(
          title,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Medium,
          color = color,
          textAlign = TextAlign.Center,
          maxLines = 2,
          modifier = Modifier.height(40.dp)
        )
      }
    }
  }
}


