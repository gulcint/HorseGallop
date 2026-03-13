@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.horsegallop.feature.safety.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.R
import com.horsegallop.core.components.HorseGallopButton
import com.horsegallop.core.components.HorseGallopTextField
import com.horsegallop.domain.safety.model.SafetyContact
import com.horsegallop.domain.safety.model.SafetySettings
import com.horsegallop.ui.theme.AppTheme
import com.horsegallop.ui.theme.LocalSemanticColors

@Composable
fun SafetyScreen(
    onBack: () -> Unit,
    viewModel: SafetyViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    val clipboard = LocalClipboardManager.current
    val locationCopiedLabel = stringResource(R.string.safety_location_copied)
    val locationUnavailableLabel = stringResource(R.string.safety_location_unavailable)

    // Pre-compute strings (cannot call stringResource inside LaunchedEffect)
    LaunchedEffect(ui.locationLink) {
        ui.locationLink?.let { link ->
            clipboard.setText(AnnotatedString(link))
            viewModel.clearLocationLink()
        }
    }

    SafetyContent(
        state = ui,
        locationCopiedLabel = locationCopiedLabel,
        locationUnavailableLabel = locationUnavailableLabel,
        onBack = onBack,
        onToggleEnabled = viewModel::toggleEnabled,
        onAddContactClick = viewModel::showAddContactSheet,
        onRemoveContact = viewModel::promptRemoveContact,
        onGenerateLocationLink = viewModel::generateLocationLink,
        onConfirmRemoveContact = viewModel::removeContact,
        onCancelRemoveContact = viewModel::cancelRemoveContact,
        onAddContactConfirm = viewModel::addContact,
        onDismissAddContact = viewModel::hideAddContactSheet
    )
}

@Composable
private fun SafetyContent(
    state: SafetyUiState,
    locationCopiedLabel: String = "",
    locationUnavailableLabel: String = "",
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onAddContactClick: () -> Unit,
    onRemoveContact: (SafetyContact) -> Unit,
    onGenerateLocationLink: () -> Unit,
    onConfirmRemoveContact: (SafetyContact) -> Unit,
    onCancelRemoveContact: () -> Unit,
    onAddContactConfirm: (String, String) -> Unit,
    onDismissAddContact: () -> Unit
) {
    val semantic = LocalSemanticColors.current

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.safety_title),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = semantic.screenTopBar
                )
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.safety_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // ── Automatic Alarm ─────────────────────────────────────────
                SafetySectionCard(
                    title = stringResource(R.string.safety_section_tracking),
                    icon = Icons.Filled.Shield
                ) {
                    Text(
                        text = stringResource(R.string.safety_enable_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.safety_enable_label),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = state.settings.isEnabled,
                            onCheckedChange = onToggleEnabled,
                            enabled = !state.isSaving
                        )
                    }
                }

                // ── Trusted Contacts ─────────────────────────────────────────
                SafetySectionCard(
                    title = stringResource(R.string.safety_section_contacts),
                    icon = Icons.Filled.People
                ) {
                    Text(
                        text = stringResource(R.string.safety_contacts_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    if (state.settings.contacts.isEmpty()) {
                        Text(
                            text = stringResource(R.string.safety_no_contacts),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    } else {
                        state.settings.contacts.forEach { contact ->
                            ContactRow(
                                contact = contact,
                                onRemove = { onRemoveContact(contact) }
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    if (state.settings.contacts.size < 5) {
                        OutlinedButton(
                            onClick = onAddContactClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Filled.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.safety_add_contact))
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.safety_max_contacts),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                // ── Live Location ─────────────────────────────────────────────
                SafetySectionCard(
                    title = stringResource(R.string.safety_section_location),
                    icon = Icons.Filled.LocationOn
                ) {
                    Text(
                        text = stringResource(R.string.safety_share_location_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    HorseGallopButton(
                        text = stringResource(R.string.safety_share_location),
                        onClick = onGenerateLocationLink,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // ── Remove contact confirmation dialog ───────────────────────────────────
    state.contactToRemove?.let { contact ->
        AlertDialog(
            onDismissRequest = onCancelRemoveContact,
            title = { Text(contact.name) },
            text = { Text(stringResource(R.string.safety_remove_contact_confirm)) },
            confirmButton = {
                TextButton(onClick = { onConfirmRemoveContact(contact) }) {
                    Text(
                        text = stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelRemoveContact) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // ── Add contact bottom sheet ──────────────────────────────────────────────
    if (state.showAddContactSheet) {
        AddContactBottomSheet(
            onDismiss = onDismissAddContact,
            onConfirm = onAddContactConfirm
        )
    }
}

// ─── Section card ─────────────────────────────────────────────────────────────

@Composable
private fun SafetySectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    val semantic = LocalSemanticColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

// ─── Contact row ──────────────────────────────────────────────────────────────

@Composable
private fun ContactRow(
    contact: SafetyContact,
    onRemove: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = contact.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.delete),
                    tint = semantic.destructive,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        HorizontalDivider(
            color = semantic.cardStroke.copy(alpha = 0.4f),
            modifier = Modifier.padding(start = 30.dp)
        )
    }
}

// ─── Add contact bottom sheet ─────────────────────────────────────────────────

@Composable
private fun AddContactBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.safety_add_contact_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            HorseGallopTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.safety_contact_name_label),
                modifier = Modifier.fillMaxWidth()
            )
            HorseGallopTextField(
                value = phone,
                onValueChange = { phone = it },
                label = stringResource(R.string.safety_contact_phone_label),
                modifier = Modifier.fillMaxWidth()
            )
            HorseGallopButton(
                text = stringResource(R.string.save),
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onConfirm(name.trim(), phone.trim())
                    }
                },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun SafetyScreenPreview() {
    AppTheme {
        SafetyContent(
            state = SafetyUiState(
                isLoading = false,
                settings = SafetySettings(
                    isEnabled = true,
                    contacts = listOf(
                        SafetyContact(id = "1", name = "Anne", phone = "+90 555 123 4567"),
                        SafetyContact(id = "2", name = "Ahmet Yılmaz", phone = "+90 555 987 6543")
                    )
                )
            ),
            onBack = {},
            onToggleEnabled = {},
            onAddContactClick = {},
            onRemoveContact = {},
            onGenerateLocationLink = {},
            onConfirmRemoveContact = {},
            onCancelRemoveContact = {},
            onAddContactConfirm = { _, _ -> },
            onDismissAddContact = {}
        )
    }
}
