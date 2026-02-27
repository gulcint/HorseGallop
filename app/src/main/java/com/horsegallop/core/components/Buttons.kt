package com.horsegallop.core.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.horsegallop.ui.theme.LocalComponentColors

enum class ButtonVariant {
    Primary,
    Secondary,
    Tonal,
    Danger
}

@Composable
fun ViewAllButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = androidx.compose.ui.res.stringResource(id = com.horsegallop.R.string.view_all)
) {
    val componentColors = LocalComponentColors.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = componentColors.buttonSecondaryContainer,
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = componentColors.buttonSecondaryContent
            )
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = componentColors.buttonSecondaryContent,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun HorseGallopButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    variant: ButtonVariant = ButtonVariant.Primary,
    containerColor: Color? = null,
    contentColor: Color? = null
) {
    val componentColors = LocalComponentColors.current
    val resolvedContainer = containerColor ?: when (variant) {
        ButtonVariant.Primary -> componentColors.buttonPrimaryContainer
        ButtonVariant.Secondary -> componentColors.buttonSecondaryContainer
        ButtonVariant.Tonal -> componentColors.buttonTonalContainer
        ButtonVariant.Danger -> componentColors.buttonDangerContainer
    }
    val resolvedContent = contentColor ?: when (variant) {
        ButtonVariant.Primary -> componentColors.buttonPrimaryContent
        ButtonVariant.Secondary -> componentColors.buttonSecondaryContent
        ButtonVariant.Tonal -> componentColors.buttonTonalContent
        ButtonVariant.Danger -> componentColors.buttonDangerContent
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = resolvedContainer,
            contentColor = resolvedContent,
            disabledContainerColor = resolvedContainer.copy(alpha = 0.5f),
            disabledContentColor = resolvedContent.copy(alpha = 0.5f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = resolvedContent,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
