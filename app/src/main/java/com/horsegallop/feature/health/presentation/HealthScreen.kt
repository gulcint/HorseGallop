package com.horsegallop.feature.health.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.horsegallop.R
import com.horsegallop.domain.health.model.HealthEvent
import com.horsegallop.domain.health.model.HealthEventType
import com.horsegallop.domain.horse.model.Horse
import com.horsegallop.ui.theme.LocalSemanticColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    viewModel: HealthViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onAddEvent: () -> Unit
) {
    val uiState by viewModel.ui.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val semantic = LocalSemanticColors.current
    val context = LocalContext.current

    // Pre-compute notification title outside LaunchedEffect (stringResource constraint)
    val reminderContentTitle = stringResource(R.string.health_reminder_upcoming_title)

    LaunchedEffect(uiState.error) {
        uiState.error?.let { msg ->
            scope.launch { snackbarHostState.showSnackbar(msg) }
            viewModel.clearError()
        }
    }

    var deleteTarget by remember { mutableStateOf<HealthEvent?>(null) }

    Scaffold(
        containerColor = semantic.screenBase,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.health_calendar_title),
                        style = MaterialTheme.typography.titleMedium,
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
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEvent,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.health_add_event),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // At filtre chip row — always visible when horses are available
            if (uiState.horses.isNotEmpty()) {
                HorseFilterChipRow(
                    horses = uiState.horses,
                    selectedHorseId = uiState.selectedHorseId,
                    onSelectAll = { viewModel.filterByHorse(null) },
                    onSelectHorse = { viewModel.filterByHorse(it) }
                )
            }

            when {
                uiState.loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
                uiState.events.isEmpty() -> {
                    HealthEmptyState(
                        modifier = Modifier.fillMaxSize(),
                        onAddClick = onAddEvent
                    )
                }
                else -> {
                    HealthContent(
                        events = uiState.events,
                        modifier = Modifier.fillMaxSize(),
                        onMarkComplete = { event ->
                            viewModel.markCompleted(event)
                        },
                        onDelete = { deleteTarget = it },
                        onScheduleReminder = { event ->
                            val title = "${event.type.name} — ${event.horseName}"
                            viewModel.scheduleReminder(context, event, title)
                        }
                    )
                }
            }
        }
    }

    deleteTarget?.let { event ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text(stringResource(R.string.horse_health_delete_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.delete(event.id)
                        viewModel.cancelReminder(context, event.id)
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(
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

@Composable
private fun HorseFilterChipRow(
    horses: List<Horse>,
    selectedHorseId: String?,
    onSelectAll: () -> Unit,
    onSelectHorse: (String) -> Unit
) {
    val semantic = LocalSemanticColors.current
    val allLabel = stringResource(R.string.health_filter_all)

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "Tümü" chip
        item(key = "chip_all", contentType = "filter_chip") {
            FilterChip(
                selected = selectedHorseId == null,
                onClick = onSelectAll,
                label = { Text(allLabel) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = semantic.chipSelected,
                    containerColor = semantic.chipUnselected
                )
            )
        }

        items(
            items = horses,
            key = { "chip_${it.id}" },
            contentType = { "filter_chip" }
        ) { horse ->
            FilterChip(
                selected = selectedHorseId == horse.id,
                onClick = { onSelectHorse(horse.id) },
                label = { Text(horse.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = semantic.chipSelected,
                    containerColor = semantic.chipUnselected
                )
            )
        }
    }
}

@Composable
private fun HealthContent(
    events: List<HealthEvent>,
    modifier: Modifier = Modifier,
    onMarkComplete: (HealthEvent) -> Unit,
    onDelete: (HealthEvent) -> Unit,
    onScheduleReminder: (HealthEvent) -> Unit = {}
) {
    val overdueEvents = events.filter { it.isOverdue }
    val dueSoonEvents = events.filter { it.isDueSoon }
    val completedEvents = events.filter { it.isCompleted }
    val normalEvents = events.filter {
        !it.isCompleted && !it.isOverdue && !it.isDueSoon
    }
    val semantic = LocalSemanticColors.current

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Summary row
        item {
            HealthSummaryRow(
                thisWeekCount = dueSoonEvents.size,
                overdueCount = overdueEvents.size,
                completedCount = completedEvents.size
            )
            Spacer(Modifier.height(8.dp))
        }

        // Overdue
        if (overdueEvents.isNotEmpty()) {
            item {
                SectionHeader(title = stringResource(R.string.health_event_overdue), count = overdueEvents.size)
                Spacer(Modifier.height(4.dp))
            }
            items(overdueEvents, key = { "overdue_${it.id}" }) { event ->
                HealthEventCard(
                    event = event,
                    onMarkComplete = { onMarkComplete(event) },
                    onDelete = { onDelete(event) },
                    onScheduleReminder = { onScheduleReminder(event) }
                )
            }
            item { Spacer(Modifier.height(4.dp)) }
        }

        // Due soon
        if (dueSoonEvents.isNotEmpty()) {
            item {
                SectionHeader(title = stringResource(R.string.health_event_due_soon), count = dueSoonEvents.size)
                Spacer(Modifier.height(4.dp))
            }
            items(dueSoonEvents, key = { "soon_${it.id}" }) { event ->
                HealthEventCard(
                    event = event,
                    onMarkComplete = { onMarkComplete(event) },
                    onDelete = { onDelete(event) },
                    onScheduleReminder = { onScheduleReminder(event) }
                )
            }
            item { Spacer(Modifier.height(4.dp)) }
        }

        // Normal upcoming
        if (normalEvents.isNotEmpty()) {
            item {
                SectionHeader(title = stringResource(R.string.horse_health_upcoming), count = null)
                Spacer(Modifier.height(4.dp))
            }
            items(normalEvents, key = { "normal_${it.id}" }) { event ->
                HealthEventCard(
                    event = event,
                    onMarkComplete = { onMarkComplete(event) },
                    onDelete = { onDelete(event) },
                    onScheduleReminder = { onScheduleReminder(event) }
                )
            }
            item { Spacer(Modifier.height(4.dp)) }
        }

        // Completed
        if (completedEvents.isNotEmpty()) {
            item {
                SectionHeader(title = stringResource(R.string.health_event_completed), count = null)
                Spacer(Modifier.height(4.dp))
            }
            items(completedEvents, key = { "done_${it.id}" }) { event ->
                HealthEventCard(
                    event = event,
                    onMarkComplete = { onMarkComplete(event) },
                    onDelete = { onDelete(event) },
                    onScheduleReminder = { onScheduleReminder(event) }
                )
            }
        }
    }
}

@Composable
private fun HealthSummaryRow(
    thisWeekCount: Int,
    overdueCount: Int,
    completedCount: Int
) {
    val semantic = LocalSemanticColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.stats_week),
            count = thisWeekCount,
            color = semantic.warning
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.health_event_overdue),
            count = overdueCount,
            color = semantic.destructive
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.health_event_completed),
            count = completedCount,
            color = semantic.success
        )
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color
) {
    val semantic = LocalSemanticColors.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        if (count != null) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun HealthEventCard(
    event: HealthEvent,
    onMarkComplete: () -> Unit,
    onDelete: () -> Unit,
    onScheduleReminder: () -> Unit = {}
) {
    val semantic = LocalSemanticColors.current
    val typeColor = event.type.semanticColor(semantic)
    val badgeColor = when {
        event.isCompleted -> semantic.success
        event.isOverdue -> semantic.destructive
        event.isDueSoon -> semantic.warning
        else -> semantic.info
    }
    val badgeText = when {
        event.isCompleted -> stringResource(R.string.health_event_completed)
        event.isOverdue -> stringResource(R.string.health_event_overdue)
        event.isDueSoon -> stringResource(R.string.health_event_due_soon)
        else -> event.scheduledDate.formatEpochDate()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = BorderStroke(
            width = if (!event.isCompleted) 1.5.dp else 1.dp,
            color = if (!event.isCompleted) typeColor.copy(alpha = 0.5f) else semantic.cardStroke
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Type icon circle
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = event.type.displayLabel(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (event.horseName.isNotBlank()) {
                    Text(
                        text = event.horseName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (event.notes.isNotBlank()) {
                    Text(
                        text = event.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                // Status badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Actions
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!event.isCompleted) {
                    IconButton(
                        onClick = onMarkComplete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = stringResource(R.string.health_mark_complete),
                            tint = semantic.success,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    // Reminder bell — only for future events
                    if (!event.isOverdue) {
                        IconButton(
                            onClick = onScheduleReminder,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.health_reminder_upcoming_title),
                                tint = semantic.info,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                IconButton(
                    onClick = onDelete,
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

@Composable
private fun HealthEmptyState(modifier: Modifier = Modifier, onAddClick: () -> Unit) {
    Box(modifier = modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("🐴", style = MaterialTheme.typography.displayLarge)
            Text(
                text = stringResource(R.string.health_calendar_empty),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.health_calendar_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Button(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.health_add_event))
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun Long.formatEpochDate(): String = runCatching {
    val sdf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    sdf.format(Date(this))
}.getOrDefault("")

@Composable
private fun HealthEventType.displayLabel(): String = when (this) {
    HealthEventType.FARRIER -> stringResource(R.string.health_event_type_farrier)
    HealthEventType.VACCINE -> stringResource(R.string.health_event_type_vaccine)
    HealthEventType.DENTAL -> stringResource(R.string.health_event_type_dental)
    HealthEventType.VET -> stringResource(R.string.health_event_type_vet)
}

private fun HealthEventType.icon(): ImageVector = when (this) {
    HealthEventType.FARRIER -> Icons.Default.Pets
    HealthEventType.VACCINE -> Icons.Default.Healing
    HealthEventType.DENTAL -> Icons.Default.Face
    HealthEventType.VET -> Icons.Default.LocalHospital
}

@Composable
private fun HealthEventType.semanticColor(semantic: com.horsegallop.ui.theme.SemanticColors): androidx.compose.ui.graphics.Color =
    when (this) {
        HealthEventType.FARRIER -> semantic.warning
        HealthEventType.VACCINE -> semantic.success
        HealthEventType.DENTAL -> semantic.info
        HealthEventType.VET -> semantic.destructive
    }

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun HealthScreenPreview() {
    val fakeEvents = listOf(
        HealthEvent(
            id = "1",
            userId = "u1",
            horseId = "h1",
            horseName = "Rüzgar",
            type = HealthEventType.FARRIER,
            scheduledDate = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L,
            notes = "6 haftada bir"
        ),
        HealthEvent(
            id = "2",
            userId = "u1",
            horseId = "h1",
            horseName = "Rüzgar",
            type = HealthEventType.VACCINE,
            scheduledDate = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L,
            notes = ""
        ),
        HealthEvent(
            id = "3",
            userId = "u1",
            horseId = "h2",
            horseName = "Fırtına",
            type = HealthEventType.VET,
            scheduledDate = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L,
            isCompleted = true,
            completedDate = System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000L
        )
    )
    MaterialTheme {
        HealthContent(
            events = fakeEvents,
            onMarkComplete = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HorseFilterChipRowPreview() {
    val fakeHorses = listOf(
        Horse(id = "h1", name = "Rüzgar"),
        Horse(id = "h2", name = "Fırtına"),
        Horse(id = "h3", name = "Yıldırım")
    )
    MaterialTheme {
        HorseFilterChipRow(
            horses = fakeHorses,
            selectedHorseId = "h1",
            onSelectAll = {},
            onSelectHorse = {}
        )
    }
}
