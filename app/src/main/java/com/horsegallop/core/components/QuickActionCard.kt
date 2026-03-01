@file:OptIn(ExperimentalMaterial3Api::class)

package com.horsegallop.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.horsegallop.ui.theme.LocalComponentColors
import com.horsegallop.ui.theme.LocalSemanticColors

@Composable
fun QuickActionCard(
  title: String,
  subtitle: String? = null,
  icon: ImageVector,
  color: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val semantic = LocalSemanticColors.current
  val component = LocalComponentColors.current
  Card(
    modifier = modifier
      .height(132.dp),
    onClick = onClick,
    colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
    shape = RoundedCornerShape(20.dp),
    border = BorderStroke(1.dp, semantic.cardStroke)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.linearGradient(
            colors = listOf(
              color.copy(alpha = 0.18f),
              semantic.cardElevated
            )
          )
        )
        .padding(16.dp)
    ) {
      Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .background(color.copy(alpha = 0.18f), CircleShape),
            contentAlignment = androidx.compose.ui.Alignment.Center
          ) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              modifier = Modifier.size(24.dp),
              tint = color
            )
          }
          Surface(
            shape = CircleShape,
            color = semantic.panelOverlay.copy(alpha = 0.78f)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              modifier = Modifier.padding(6.dp).size(14.dp),
              tint = component.tintMuted
            )
          }
        }
        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
          if (subtitle != null) {
            Text(
              text = subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}
