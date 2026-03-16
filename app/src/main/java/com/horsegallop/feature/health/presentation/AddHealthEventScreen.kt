package com.horsegallop.feature.health.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.horsegallop.R
import com.horsegallop.domain.health.model.HealthEvent
import com.horsegallop.domain.health.model.HealthEventType
import com.horsegallop.domain.horse.model.Horse
import com.horsegallop.feature.horse.presentation.HorseViewModel
import com.horsegallop.ui.theme.LocalSemanticColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddHealthEventScreen(
    healthViewModel: HealthViewModel = hiltViewModel(),
    horseViewModel: HorseViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val healthUiState by healthViewModel.ui.collectAsState()
    val horseUiState by horseViewModel.uiState.collectAsState()
    val semantic = LocalSemanticColors.current

    var selectedType by rememberSaveable { mutableStateOf(HealthEventType.FARRIER) }
    var selectedHorse by rememberSaveable { mutableStateOf<Horse?>(null) }
    var scheduledDateMs by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var notes by rememberSaveable { mutableStateOf("") }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var horseMenuExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.health_add_event),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Event type selector
            Text(
                text = stringResource(R.string.horse_health_type_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HealthEventType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = {
                            Text(
                                type.labelText(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Horse selector dropdown
            Text(
                text = stringResource(R.string.health_select_horse_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ExposedDropdownMenuBox(
                expanded = horseMenuExpanded,
                onExpandedChange = { horseMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedHorse?.name ?: stringResource(R.string.health_select_horse_placeholder),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    label = { Text(stringResource(R.string.health_horse_field_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = horseMenuExpanded) },
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = horseMenuExpanded,
                    onDismissRequest = { horseMenuExpanded = false }
                ) {
                    horseUiState.horses.forEach { horse ->
                        DropdownMenuItem(
                            text = { Text(horse.name) },
                            onClick = {
                                selectedHorse = horse
                                horseMenuExpanded = false
                            }
                        )
                    }
                    if (horseUiState.horses.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.health_horse_not_found)) },
                            onClick = { horseMenuExpanded = false },
                            enabled = false
                        )
                    }
                }
            }

            // Date picker trigger
            OutlinedTextField(
                value = scheduledDateMs.formatEpochDate(),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.horse_health_date_label)) },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.horse_health_notes_label)) },
                placeholder = { Text(stringResource(R.string.horse_health_notes_hint)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    val horse = selectedHorse ?: return@Button
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@Button
                    val event = HealthEvent(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        horseId = horse.id,
                        horseName = horse.name,
                        type = selectedType,
                        scheduledDate = scheduledDateMs,
                        notes = notes.trim()
                    )
                    healthViewModel.saveEvent(event)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedHorse != null && !healthUiState.isSaving
            ) {
                if (healthUiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.horse_health_save))
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = scheduledDateMs
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { ms ->
                        scheduledDateMs = ms
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun Long.formatEpochDate(): String = runCatching {
    val sdf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    sdf.format(Date(this))
}.getOrDefault("")

@Composable
private fun HealthEventType.labelText(): String = when (this) {
    HealthEventType.FARRIER -> stringResource(R.string.health_event_type_farrier)
    HealthEventType.VACCINE -> stringResource(R.string.health_event_type_vaccine)
    HealthEventType.DENTAL -> stringResource(R.string.health_event_type_dental)
    HealthEventType.VET -> stringResource(R.string.health_event_type_vet)
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Preview(showBackground = true)
@Composable
private fun AddHealthEventScreenPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Etkinlik Ekle",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Etkinlik Türü",
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HealthEventType.entries.forEach { type ->
                        FilterChip(
                            selected = type == HealthEventType.FARRIER,
                            onClick = {},
                            label = { Text(type.name) }
                        )
                    }
                }
                OutlinedTextField(
                    value = "Rüzgar",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("At") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = "15 Mar 2026",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tarih") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                    Text("Kaydet")
                }
            }
        }
    }
}
