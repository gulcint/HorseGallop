@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.horsegallop.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.R
import com.horsegallop.settings.AppLanguage
import com.horsegallop.settings.ThemeMode
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.horsegallop.core.feedback.LocalAppFeedbackController
import com.horsegallop.ui.theme.LocalSemanticColors

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val privacyState by viewModel.privacyState.collectAsState()
    val contentState by viewModel.contentState.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val feedback = LocalAppFeedbackController.current
    val clipboard = LocalClipboardManager.current
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val semantic = LocalSemanticColors.current
    val settingsControlsEnabled = !syncState.isInitialSyncRunning && !syncState.isSaving

    LaunchedEffect(privacyState.errorMessageResId) {
        privacyState.errorMessageResId?.let { messageResId ->
            feedback.showError(messageResId)
            viewModel.clearPrivacyError()
        }
    }

    LaunchedEffect(privacyState.exportJson) {
        privacyState.exportJson?.let { json ->
            clipboard.setText(AnnotatedString(json))
            feedback.showSuccess(R.string.privacy_export_ready)
            viewModel.consumeExport()
        }
    }

    LaunchedEffect(syncState.remoteErrorMessageResId) {
        syncState.remoteErrorMessageResId?.let { messageResId ->
            feedback.showError(messageResId)
            viewModel.consumeRemoteError()
        }
    }

    LaunchedEffect(syncState.saveErrorMessageResId) {
        syncState.saveErrorMessageResId?.let { messageResId ->
            feedback.showError(messageResId)
            viewModel.consumeSaveError()
        }
    }

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = semantic.screenTopBar,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimensionResource(id = R.dimen.padding_screen_horizontal))
                .padding(bottom = dimensionResource(id = R.dimen.padding_screen_vertical)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.section_spacing_md))
        ) {
            if (syncState.isInitialSyncRunning || syncState.isSaving) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = semantic.cardSubtle),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, semantic.cardStroke)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(
                                id = if (syncState.isInitialSyncRunning) R.string.settings_sync_loading
                                else R.string.settings_sync_saving
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            SettingsSectionCard(
                title = stringResource(id = R.string.setting_theme_title),
                subtitle = contentState.themeSubtitle,
                icon = Icons.Filled.Palette
            ) {
                SettingsRadioRow(
                    label = stringResource(id = R.string.theme_system),
                    selected = state.themeMode == ThemeMode.SYSTEM,
                    enabled = settingsControlsEnabled,
                    onClick = { viewModel.onThemeSelected(ThemeMode.SYSTEM) }
                )
                SettingsRadioRow(
                    label = stringResource(id = R.string.theme_light),
                    selected = state.themeMode == ThemeMode.LIGHT,
                    enabled = settingsControlsEnabled,
                    onClick = { viewModel.onThemeSelected(ThemeMode.LIGHT) }
                )
                SettingsRadioRow(
                    label = stringResource(id = R.string.theme_dark),
                    selected = state.themeMode == ThemeMode.DARK,
                    enabled = settingsControlsEnabled,
                    onClick = { viewModel.onThemeSelected(ThemeMode.DARK) }
                )
            }

            SettingsSectionCard(
                title = stringResource(id = R.string.setting_language_title),
                subtitle = contentState.languageSubtitle,
                icon = Icons.Filled.Language
            ) {
                SettingsRadioRow(
                    label = stringResource(id = R.string.setting_language_system),
                    selected = state.language == AppLanguage.SYSTEM,
                    enabled = settingsControlsEnabled,
                    onClick = { viewModel.onLanguageSelected(AppLanguage.SYSTEM) }
                )
                SettingsRadioRow(
                    label = stringResource(id = R.string.setting_language_english),
                    selected = state.language == AppLanguage.ENGLISH,
                    enabled = settingsControlsEnabled,
                    onClick = { viewModel.onLanguageSelected(AppLanguage.ENGLISH) }
                )
                SettingsRadioRow(
                    label = stringResource(id = R.string.setting_language_turkish),
                    selected = state.language == AppLanguage.TURKISH,
                    enabled = settingsControlsEnabled,
                    onClick = { viewModel.onLanguageSelected(AppLanguage.TURKISH) }
                )
            }

            SettingsSectionCard(
                title = stringResource(id = R.string.setting_notifications_title),
                subtitle = contentState.notificationsSubtitle,
                icon = Icons.Filled.Notifications
            ) {
                SettingsSwitchRow(
                    label = stringResource(id = R.string.setting_notifications_subtitle),
                    checked = state.notificationsEnabled,
                    enabled = settingsControlsEnabled,
                    onCheckedChange = viewModel::onNotificationsChanged
                )
            }

            SettingsSectionCard(
                title = stringResource(id = R.string.setting_privacy_title),
                subtitle = contentState.privacySubtitle,
                icon = Icons.Filled.Shield
            ) {
                SettingsActionRow(
                    title = stringResource(id = R.string.privacy_export_title),
                    subtitle = stringResource(id = R.string.privacy_export_subtitle),
                    actionLabel = stringResource(id = R.string.privacy_export_action),
                    isDestructive = false,
                    enabled = !privacyState.isProcessing,
                    onClick = viewModel::requestDataExport
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsActionRow(
                    title = stringResource(id = R.string.delete_account),
                    subtitle = stringResource(id = R.string.privacy_delete_subtitle),
                    actionLabel = stringResource(id = R.string.delete),
                    isDestructive = true,
                    enabled = !privacyState.isProcessing,
                    onClick = { showDeleteConfirm = true }
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(text = stringResource(id = R.string.delete_account)) },
            text = { Text(text = stringResource(id = R.string.delete_account_confirmation)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.requestAccountDeletion(onAccountDeleted)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = semantic.destructive)
                ) {
                    Text(text = stringResource(id = R.string.delete))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    val semantic = LocalSemanticColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_xl)),
        border = androidx.compose.foundation.BorderStroke(1.dp, semantic.cardStroke),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.elevation_sm))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_card_md))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            subtitle?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = semantic.cardSubtle,
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg))
                    )
                    .padding(vertical = 8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsRadioRow(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick, enabled = enabled)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsSectionCard(
            title = "Theme",
            icon = Icons.Filled.Palette
        ) {}
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    actionLabel: String,
    isDestructive: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        val buttonColors = if (isDestructive) {
            ButtonDefaults.buttonColors(containerColor = semantic.destructive)
        } else {
            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        }
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = buttonColors,
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(text = actionLabel)
        }
    }
}
