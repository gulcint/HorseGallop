package com.horsegallop.feature.horse.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.R
import com.horsegallop.domain.horse.model.HorseHealthEvent
import com.horsegallop.domain.horse.model.HorseHealthEventType
import com.horsegallop.ui.theme.HealthColorDeworming
import com.horsegallop.ui.theme.HealthColorDental
import com.horsegallop.ui.theme.HealthColorFarrier
import com.horsegallop.ui.theme.HealthColorOther
import com.horsegallop.ui.theme.HealthColorVaccination
import com.horsegallop.ui.theme.HealthColorVet
import com.horsegallop.ui.theme.LocalSemanticColors
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorseHealthScreen(
    horseId: String,
    horseName: String,
    viewModel: HorseHealthViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.ui.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<HorseHealthEvent?>(null) }

    LaunchedEffect(horseId) {
        viewModel.load(horseId, horseName)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { msg ->
            scope.launch { snackbarHostState.showSnackbar(msg) }
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = LocalSemanticColors.current.screenBase,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.horse_health_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (horseName.isNotBlank()) {
                            Text(
                                text = horseName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.horse_health_add), tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            uiState.events.isEmpty() -> {
                HorseHealthEmptyState(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    onAddClick = { showAddSheet = true }
                )
            }
            else -> {
                HorseHealthContent(
                    uiState = uiState,
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    onDeleteClick = { deleteTarget = it }
                )
            }
        }
    }

    // ── Add Event Sheet ──
    if (showAddSheet) {
        AddHealthEventSheet(
            isSaving = uiState.isSaving,
            onDismiss = { showAddSheet = false },
            onSave = { type, date, notes ->
                viewModel.addEvent(type, date, notes)
                showAddSheet = false
            }
        )
    }

    // ── Delete Confirm Dialog ──
    deleteTarget?.let { event ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text(stringResource(R.string.horse_health_delete_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEvent(event.id)
                        deleteTarget = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

// ─── Content ─────────────────────────────────────────────────────────────────

@Composable
private fun HorseHealthContent(
    uiState: HorseHealthUiState,
    modifier: Modifier = Modifier,
    onDeleteClick: (HorseHealthEvent) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Upcoming section
        if (uiState.upcomingEvents.isNotEmpty()) {
            item {
                SectionHeader(
                    title = stringResource(R.string.horse_health_upcoming),
                    badge = uiState.upcomingEvents.size
                )
                Spacer(Modifier.height(6.dp))
            }
            items(uiState.upcomingEvents, key = { "up_${it.id}" }) { event ->
                HealthEventCard(event = event, isUpcoming = true, onDeleteClick = onDeleteClick)
            }
            item { Spacer(Modifier.height(8.dp)) }
        }

        // Past section
        if (uiState.pastEvents.isNotEmpty()) {
            item {
                SectionHeader(title = stringResource(R.string.horse_health_past))
                Spacer(Modifier.height(6.dp))
            }
            items(uiState.pastEvents, key = { "past_${it.id}" }) { event ->
                HealthEventCard(event = event, isUpcoming = false, onDeleteClick = onDeleteClick)
            }
        }
    }
}

// ─── Section Header ───────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String, badge: Int? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (badge != null) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Text(
                    text = badge.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// ─── Event Card ───────────────────────────────────────────────────────────────

@Composable
private fun HealthEventCard(
    event: HorseHealthEvent,
    isUpcoming: Boolean,
    onDeleteClick: (HorseHealthEvent) -> Unit
) {
    val semantic = LocalSemanticColors.current
    val typeColor = event.type.color()
    val daysLeft = event.daysUntil()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = BorderStroke(
            width = if (isUpcoming) 1.5.dp else 1.dp,
            color = if (isUpcoming) typeColor.copy(alpha = 0.6f) else semantic.cardStroke
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Type badge circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(typeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = event.type.icon(),
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Content
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = event.type.displayLabel(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = event.date.formatDisplayDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (event.notes.isNotBlank()) {
                    Text(
                        text = event.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Days badge + delete
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DaysBadge(daysLeft = daysLeft, typeColor = typeColor)
                IconButton(
                    onClick = { onDeleteClick(event) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ─── Days Badge ───────────────────────────────────────────────────────────────

@Composable
private fun DaysBadge(daysLeft: Long, typeColor: Color) {
    val (bgColor, textColor, label) = when {
        daysLeft == 0L -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.primary,
            stringResource(R.string.horse_health_today)
        )
        daysLeft > 0 -> Triple(
            typeColor.copy(alpha = 0.12f),
            typeColor,
            stringResource(R.string.horse_health_days_left, daysLeft)
        )
        else -> Triple(
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
            MaterialTheme.colorScheme.onSurfaceVariant,
            stringResource(R.string.horse_health_days_ago, -daysLeft)
        )
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun HorseHealthEmptyState(modifier: Modifier = Modifier, onAddClick: () -> Unit) {
    Box(modifier = modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("🩺", style = MaterialTheme.typography.displayLarge)
            Text(
                text = stringResource(R.string.horse_health_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.horse_health_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Button(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.horse_health_add))
            }
        }
    }
}

// ─── Add Event Bottom Sheet ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddHealthEventSheet(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (HorseHealthEventType, String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var selectedType by rememberSaveable { mutableStateOf(HorseHealthEventType.FARRIER) }
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var notes by rememberSaveable { mutableStateOf("") }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LocalSemanticColors.current.cardElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.horse_health_add),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Type selector
            Text(
                text = stringResource(R.string.horse_health_type_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorseHealthEventType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.displayLabel(), style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = {
                            Icon(
                                imageVector = type.icon(),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = type.color().copy(alpha = 0.18f),
                            selectedLabelColor = type.color(),
                            selectedLeadingIconColor = type.color()
                        )
                    )
                }
            }

            // Date picker trigger
            OutlinedTextField(
                value = selectedDate.formatDisplayDate(),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.horse_health_date_label)) },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = stringResource(R.string.cd_pick_date))
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

            // Save button
            com.horsegallop.core.components.HorseGallopButton(
                text = stringResource(R.string.horse_health_save),
                onClick = { onSave(selectedType, selectedDate, notes) },
                modifier = Modifier.fillMaxWidth(),
                isLoading = isSaving
            )
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = runCatching {
                LocalDate.parse(selectedDate)
                    .atStartOfDay(ZoneId.of("UTC"))
                    .toInstant()
                    .toEpochMilli()
            }.getOrDefault(System.currentTimeMillis())
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                            .toString()
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

private fun String.formatDisplayDate(): String = runCatching {
    val parsed = LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
    parsed.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
}.getOrDefault(this)

@Composable
private fun HorseHealthEventType.displayLabel(): String = when (this) {
    HorseHealthEventType.FARRIER -> stringResource(R.string.horse_health_type_farrier)
    HorseHealthEventType.VACCINATION -> stringResource(R.string.horse_health_type_vaccination)
    HorseHealthEventType.DENTAL -> stringResource(R.string.horse_health_type_dental)
    HorseHealthEventType.VET -> stringResource(R.string.horse_health_type_vet)
    HorseHealthEventType.DEWORMING -> stringResource(R.string.horse_health_type_deworming)
    HorseHealthEventType.OTHER -> stringResource(R.string.horse_health_type_other)
}

private fun HorseHealthEventType.icon(): ImageVector = when (this) {
    HorseHealthEventType.FARRIER -> Icons.Default.Star
    HorseHealthEventType.VACCINATION -> Icons.Default.FavoriteBorder
    HorseHealthEventType.DENTAL -> Icons.Default.Spa
    HorseHealthEventType.VET -> Icons.Default.LocalHospital
    HorseHealthEventType.DEWORMING -> Icons.Default.MedicalServices
    HorseHealthEventType.OTHER -> Icons.Default.CalendarMonth
}

@Composable
private fun HorseHealthEventType.color(): Color = when (this) {
    HorseHealthEventType.FARRIER     -> HealthColorFarrier
    HorseHealthEventType.VACCINATION -> HealthColorVaccination
    HorseHealthEventType.DENTAL      -> HealthColorDental
    HorseHealthEventType.VET         -> HealthColorVet
    HorseHealthEventType.DEWORMING   -> HealthColorDeworming
    HorseHealthEventType.OTHER       -> HealthColorOther
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun HorseHealthScreenPreview() {
    val fakeState = HorseHealthUiState(
        isLoading = false,
        horseName = "Rüzgar",
        upcomingEvents = listOf(
            HorseHealthEvent(
                id = "1",
                horseId = "h1",
                type = HorseHealthEventType.FARRIER,
                date = LocalDate.now().plusDays(5).toString(),
                notes = "6 haftada bir"
            ),
            HorseHealthEvent(
                id = "2",
                horseId = "h1",
                type = HorseHealthEventType.VACCINATION,
                date = LocalDate.now().plusDays(12).toString(),
                notes = ""
            )
        ),
        pastEvents = listOf(
            HorseHealthEvent(
                id = "3",
                horseId = "h1",
                type = HorseHealthEventType.VET,
                date = LocalDate.now().minusDays(20).toString(),
                notes = "Rutin kontrol"
            )
        ),
        events = emptyList()
    )
    MaterialTheme {
        HorseHealthContent(uiState = fakeState, onDeleteClick = {})
    }
}
