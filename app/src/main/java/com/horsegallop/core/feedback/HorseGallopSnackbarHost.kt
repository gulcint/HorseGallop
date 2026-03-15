package com.horsegallop.core.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.horsegallop.R
import com.horsegallop.ui.theme.LocalSemanticColors

data class AppSnackbarVisuals(
    override val message: String,
    val tone: FeedbackTone,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short
) : SnackbarVisuals

@Composable
fun HorseGallopSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current

    SnackbarHost(
        hostState = hostState,
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) { snackbarData ->
        val visuals = snackbarData.visuals as? AppSnackbarVisuals
        val tone = visuals?.tone ?: FeedbackTone.Info
        val strokeColor = when (tone) {
            FeedbackTone.Success -> semantic.calloutBorderSuccess
            FeedbackTone.Error -> semantic.calloutBorderError
            FeedbackTone.Warning -> semantic.calloutBorderWarning
            FeedbackTone.Info -> semantic.calloutBorderInfo
        }
        val icon = when (tone) {
            FeedbackTone.Success -> Icons.Filled.CheckCircle
            FeedbackTone.Error -> Icons.Filled.ErrorOutline
            FeedbackTone.Warning -> Icons.Filled.WarningAmber
            FeedbackTone.Info -> Icons.Filled.Info
        }

        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = semantic.cardElevated,
                border = BorderStroke(1.dp, semantic.cardStroke),
                shadowElevation = 14.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.mipmap.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(36.dp)
                            .background(strokeColor, RoundedCornerShape(2.dp))
                    )
                    ToneIcon(icon = icon, tint = strokeColor, modifier = Modifier.size(18.dp))
                    Text(
                        text = snackbarData.visuals.message,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    snackbarData.visuals.actionLabel?.let { label ->
                        TextButton(onClick = { snackbarData.performAction() }) {
                            Text(text = label, color = strokeColor)
                        }
                    }
                    if (snackbarData.visuals.withDismissAction) {
                        IconButton(onClick = { snackbarData.dismiss() }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToneIcon(
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = modifier
    )
}
