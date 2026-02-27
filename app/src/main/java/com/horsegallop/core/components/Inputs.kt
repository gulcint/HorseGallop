package com.horsegallop.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.horsegallop.R
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import com.horsegallop.ui.theme.LocalComponentColors
import com.horsegallop.ui.theme.LocalSemanticColors

enum class InputFieldVariant {
    Elevated,
    Subtle
}

@Composable
fun AutoRideDetectionSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val semantic = LocalSemanticColors.current
    val component = LocalComponentColors.current
    Card(
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.auto_detect_ride),
                style = MaterialTheme.typography.bodySmall
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = component.tintStrong)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorseGallopDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    options: List<String>,
    label: String? = null,
    modifier: Modifier = Modifier,
    placeholder: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val semantic = LocalSemanticColors.current
    val component = LocalComponentColors.current

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = if (label != null) {
                { Text(label, style = MaterialTheme.typography.bodySmall) }
            } else null,
            placeholder = if (placeholder != null) {
                { Text(placeholder, style = MaterialTheme.typography.bodySmall) }
            } else null,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(
                    type = MenuAnchorType.PrimaryNotEditable,
                    enabled = true
                )
                .fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = component.inputBorderFocused,
                unfocusedBorderColor = component.inputBorderUnfocused,
                focusedContainerColor = component.inputContainer,
                unfocusedContainerColor = component.inputContainer
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(semantic.panelOverlay)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.bodyMedium) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    },
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 0.dp
                    )
                )
            }
        }
    }
}

@Composable
fun HorseGallopDatePicker(
    value: String,
    onDateSelected: () -> Unit,
    label: String? = null,
    modifier: Modifier = Modifier,
    placeholder: String? = null
) {
    val semantic = LocalSemanticColors.current
    val component = LocalComponentColors.current
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = if (label != null) {
                { Text(label, style = MaterialTheme.typography.bodySmall) }
            } else null,
            placeholder = if (placeholder != null) {
                { Text(placeholder, style = MaterialTheme.typography.bodySmall) }
            } else null,
            trailingIcon = {
                Icon(
                    Icons.Filled.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = component.inputBorderFocused,
                unfocusedBorderColor = component.inputBorderUnfocused,
                focusedContainerColor = component.inputContainer,
                unfocusedContainerColor = component.inputContainer
            )
        )
        // Overlay for click detection
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(24.dp))
                .clickable { onDateSelected() }
        )
    }
}

@Composable
fun HorseGallopSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit = {},
    placeholder: String? = null,
    modifier: Modifier = Modifier
) {
    val component = LocalComponentColors.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        label = null,
        placeholder = if (placeholder != null) {
            { Text(placeholder, style = MaterialTheme.typography.bodySmall) }
        } else null,
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onQueryChange("") }
                )
            }
        },
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = component.inputBorderFocused,
            unfocusedBorderColor = component.inputBorderUnfocused,
            focusedContainerColor = component.inputContainer,
            unfocusedContainerColor = component.inputContainer
        ),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
    )
}

@Composable
fun HorseGallopTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    variant: InputFieldVariant = InputFieldVariant.Elevated,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true
) {
    val component = LocalComponentColors.current
    val container = when (variant) {
        InputFieldVariant.Elevated -> component.inputContainer
        InputFieldVariant.Subtle -> component.inputContainerSubtle
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        modifier = modifier,
        isError = isError,
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = component.inputBorderFocused,
            unfocusedBorderColor = component.inputBorderUnfocused,
            focusedContainerColor = container,
            unfocusedContainerColor = container
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}
