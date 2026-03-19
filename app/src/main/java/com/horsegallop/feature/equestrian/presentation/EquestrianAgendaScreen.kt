@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.horsegallop.feature.equestrian.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.horsegallop.R
import com.horsegallop.core.components.ButtonVariant
import com.horsegallop.core.components.HorseGallopButton
import com.horsegallop.domain.equestrian.model.EquestrianAnnouncement
import com.horsegallop.domain.equestrian.model.EquestrianCompetition
import com.horsegallop.domain.equestrian.model.FederationSourceHealthItem
import com.horsegallop.domain.equestrian.model.FederatedBarnSyncStatus
import com.horsegallop.ui.theme.LocalSemanticColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun EquestrianAgendaScreen(
    onBack: () -> Unit,
    initialTab: EquestrianAgendaTab = EquestrianAgendaTab.ANNOUNCEMENTS,
    onTbfEventClick: (venueCode: String, eventIndex: Int) -> Unit = { _, _ -> },
    onNavigateToCalendar: () -> Unit = {},
    viewModel: EquestrianAgendaViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current
    val semantic = LocalSemanticColors.current

    androidx.compose.runtime.LaunchedEffect(initialTab) {
        viewModel.selectTab(initialTab)
    }

    state.previewItem?.let { previewItem ->
        AgendaPreviewSheet(
            previewItem = previewItem,
            onDismiss = viewModel::dismissPreview,
            onOpenSource = { url ->
                if (url.isNotBlank()) {
                    uriHandler.openUri(url)
                }
            }
        )
    }

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.equestrian_agenda_title), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = semantic.screenTopBar
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SyncStatusCard(
                syncStatus = state.syncStatus,
                sourceHealth = state.sourceHealth,
                isLoading = state.isLoadingSyncStatus,
                isLoadingHealth = state.isLoadingSourceHealth,
                error = state.syncStatusError,
                sourceHealthError = state.sourceHealthError,
                isTriggering = state.isTriggeringSync,
                actionMessage = state.syncActionMessage,
                onTriggerSync = viewModel::triggerManualSync,
                onTriggerDebugSync = viewModel::triggerDebugSync
            )
            TabRow(selectedTabIndex = state.selectedTab.ordinal) {
                Tab(
                    selected = state.selectedTab == EquestrianAgendaTab.ANNOUNCEMENTS,
                    onClick = { viewModel.selectTab(EquestrianAgendaTab.ANNOUNCEMENTS) },
                    text = { Text(stringResource(R.string.equestrian_agenda_tab_announcements)) }
                )
                Tab(
                    selected = state.selectedTab == EquestrianAgendaTab.COMPETITIONS,
                    onClick = { viewModel.selectTab(EquestrianAgendaTab.COMPETITIONS) },
                    text = { Text(stringResource(R.string.equestrian_agenda_tab_competitions)) }
                )
                Tab(
                    selected = state.selectedTab == EquestrianAgendaTab.TBF,
                    onClick = { viewModel.selectTab(EquestrianAgendaTab.TBF) },
                    text = { Text(stringResource(R.string.equestrian_tab_tbf)) }
                )
            }

            when (state.selectedTab) {
                EquestrianAgendaTab.ANNOUNCEMENTS -> FeedState(
                    loading = state.isLoadingAnnouncements,
                    error = state.announcementsError,
                    isEmpty = state.announcements.isEmpty(),
                    emptyMessage = stringResource(R.string.equestrian_agenda_empty_announcements)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.announcements, key = { it.id }) { item ->
                            AnnouncementCard(
                                item = item,
                                onOpen = { viewModel.showAnnouncementPreview(item) }
                            )
                        }
                    }
                }

                EquestrianAgendaTab.COMPETITIONS -> FeedState(
                    loading = state.isLoadingCompetitions,
                    error = state.competitionsError,
                    isEmpty = state.competitions.isEmpty(),
                    emptyMessage = stringResource(R.string.equestrian_agenda_empty_competitions)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.competitions, key = { it.id }) { item ->
                            CompetitionCard(
                                item = item,
                                onOpen = { viewModel.showCompetitionPreview(item) }
                            )
                        }
                    }
                }

                EquestrianAgendaTab.TBF -> {
                    val tbfViewModel: com.horsegallop.feature.tbf.presentation.TbfViewModel = hiltViewModel()
                    OutlinedButton(
                        onClick = onNavigateToCalendar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.tbf_view_calendar))
                    }
                    TbfTabContent(
                        viewModel = tbfViewModel,
                        onEventClick = onTbfEventClick
                    )
                }
            }
        }
    }
}

@Composable
private fun AgendaPreviewSheet(
    previewItem: AgendaPreviewItem,
    onDismiss: () -> Unit,
    onOpenSource: (String) -> Unit
) {
    val semantic = LocalSemanticColors.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = semantic.cardElevated
    ) {
        when (previewItem) {
            is AgendaPreviewItem.Announcement -> AnnouncementPreviewContent(
                item = previewItem.item,
                onOpenSource = onOpenSource
            )
            is AgendaPreviewItem.Competition -> CompetitionPreviewContent(
                item = previewItem.item,
                onOpenSource = onOpenSource
            )
        }
    }
}

@Composable
private fun SyncStatusCard(
    syncStatus: FederatedBarnSyncStatus?,
    sourceHealth: List<FederationSourceHealthItem>,
    isLoading: Boolean,
    isLoadingHealth: Boolean,
    error: String?,
    sourceHealthError: String?,
    isTriggering: Boolean,
    actionMessage: SyncActionMessage?,
    onTriggerSync: () -> Unit,
    onTriggerDebugSync: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(18.dp),
        color = semantic.cardElevated,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Sync,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.equestrian_agenda_sync_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when {
                            isLoading -> stringResource(R.string.equestrian_agenda_sync_loading)
                            error != null -> stringResource(R.string.equestrian_agenda_sync_error)
                            syncStatus == null -> stringResource(R.string.equestrian_agenda_sync_idle)
                            syncStatus.status == "success" && syncStatus.syncedAt.isNotBlank() ->
                                stringResource(
                                    R.string.equestrian_agenda_sync_success,
                                    syncStatus.itemCount,
                                    syncStatus.syncedAt.toAgendaSyncLabel()
                                )
                            syncStatus.status == "error" ->
                                syncStatus.errorMessage ?: stringResource(R.string.equestrian_agenda_sync_error)
                            else -> stringResource(R.string.equestrian_agenda_sync_idle)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (actionMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(
                                when (actionMessage) {
                                    SyncActionMessage.REFRESHED -> R.string.equestrian_agenda_sync_refreshed
                                    SyncActionMessage.THROTTLED -> R.string.equestrian_agenda_sync_throttled
                                    SyncActionMessage.DEBUG_REFRESHED -> R.string.equestrian_agenda_sync_debug_refreshed
                                }
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                TextButton(
                    onClick = onTriggerSync,
                    enabled = !isTriggering && !isLoading
                ) {
                    if (isTriggering) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp).height(16.dp), strokeWidth = 2.dp)
                    }
                    Text(text = stringResource(R.string.equestrian_agenda_sync_now))
                }
            }

            TextButton(
                onClick = onTriggerDebugSync,
                enabled = !isTriggering
            ) {
                Text(text = stringResource(R.string.equestrian_agenda_sync_force_refresh))
            }

            FederationSourceHealthRow(
                items = sourceHealth,
                isLoading = isLoadingHealth,
                error = sourceHealthError
            )
        }
    }
}

@Composable
private fun FederationSourceHealthRow(
    items: List<FederationSourceHealthItem>,
    isLoading: Boolean,
    error: String?
) {
    if (isLoading) {
        Text(
            text = stringResource(R.string.equestrian_agenda_health_loading),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
    if (error != null) {
        Text(
            text = stringResource(R.string.equestrian_agenda_health_error),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            FederationSourceHealthPill(
                item = item,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FederationSourceHealthPill(
    item: FederationSourceHealthItem,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val toneColor = when (item.status) {
        "success" -> MaterialTheme.colorScheme.primary
        "stale" -> semantic.warning
        "error" -> semantic.destructive
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = semantic.cardSubtle
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = when (item.source) {
                    "barns" -> stringResource(R.string.equestrian_agenda_health_barns)
                    "announcements" -> stringResource(R.string.equestrian_agenda_health_announcements)
                    "competitions" -> stringResource(R.string.equestrian_agenda_health_competitions)
                    else -> item.source
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = when {
                    item.status == "error" && !item.errorMessage.isNullOrBlank() ->
                        stringResource(R.string.equestrian_agenda_health_failed)
                    item.status == "stale" ->
                        stringResource(R.string.equestrian_agenda_health_stale, item.dataAgeMinutes)
                    item.dataAgeMinutes >= 0 ->
                        stringResource(R.string.equestrian_agenda_health_age_minutes, item.dataAgeMinutes)
                    else -> stringResource(R.string.equestrian_agenda_health_waiting)
                },
                style = MaterialTheme.typography.bodySmall,
                color = toneColor
            )
            Text(
                text = stringResource(R.string.equestrian_agenda_health_item_count, item.itemCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeedState(
    loading: Boolean,
    error: String?,
    isEmpty: Boolean,
    emptyMessage: String,
    content: @Composable () -> Unit
) {
    val semantic = LocalSemanticColors.current
    when {
        loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        error != null -> Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = semantic.cardElevated,
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.WifiOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.equestrian_agenda_error_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.equestrian_agenda_error_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        isEmpty -> Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = semantic.cardElevated,
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Campaign,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Text(
                        text = emptyMessage,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.equestrian_agenda_empty_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        else -> content()
    }
}

@Composable
private fun AnnouncementPreviewContent(
    item: EquestrianAnnouncement,
    onOpenSource: (String) -> Unit
) {
    PreviewSheetLayout(
        chipLabel = stringResource(R.string.equestrian_agenda_chip_announcements),
        title = item.title,
        meta = item.publishedAtLabel,
        summary = item.summary.ifBlank { stringResource(R.string.equestrian_agenda_preview_summary_fallback) },
        secondaryMeta = null,
        imageUrl = item.imageUrl,
        onOpenSource = { onOpenSource(item.detailUrl) }
    )
}

@Composable
private fun CompetitionPreviewContent(
    item: EquestrianCompetition,
    onOpenSource: (String) -> Unit
) {
    PreviewSheetLayout(
        chipLabel = stringResource(R.string.equestrian_agenda_chip_competitions),
        title = item.title,
        meta = item.dateLabel,
        summary = item.location.ifBlank { stringResource(R.string.equestrian_agenda_preview_competition_fallback) },
        secondaryMeta = item.location.takeIf { it.isNotBlank() },
        imageUrl = null,
        onOpenSource = { onOpenSource(item.detailUrl) }
    )
}

@Composable
private fun PreviewSheetLayout(
    chipLabel: String,
    title: String,
    meta: String,
    summary: String,
    secondaryMeta: String?,
    imageUrl: String?,
    onOpenSource: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Text(
                text = chipLabel,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(semantic.panelOverlay)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        if (meta.isNotBlank()) {
            Text(
                text = meta,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        secondaryMeta?.takeIf { it.isNotBlank() && it != summary }?.let {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = semantic.cardSubtle
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorseGallopButton(
            text = stringResource(R.string.equestrian_agenda_preview_open_source),
            onClick = onOpenSource,
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.Primary
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TbfTabContent(
    viewModel: com.horsegallop.feature.tbf.presentation.TbfViewModel,
    onEventClick: (venueCode: String, eventIndex: Int) -> Unit
) {
    val state by viewModel.ui.collectAsState()
    val semantic = LocalSemanticColors.current

    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        state.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = state.error ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        else -> {
            Column(modifier = Modifier.fillMaxSize()) {
                val selectedTabIndex = if (state.viewMode == com.horsegallop.feature.tbf.presentation.TbfViewMode.PROGRAM) 0 else 1
                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = state.viewMode == com.horsegallop.feature.tbf.presentation.TbfViewMode.PROGRAM,
                        onClick = { viewModel.switchMode(com.horsegallop.feature.tbf.presentation.TbfViewMode.PROGRAM) },
                        text = { Text(stringResource(R.string.tbf_program_tab)) }
                    )
                    Tab(
                        selected = state.viewMode == com.horsegallop.feature.tbf.presentation.TbfViewMode.RESULTS,
                        onClick = { viewModel.switchMode(com.horsegallop.feature.tbf.presentation.TbfViewMode.RESULTS) },
                        text = { Text(stringResource(R.string.tbf_results_tab)) }
                    )
                }

                if (state.venues.isNotEmpty()) {
                    androidx.compose.foundation.lazy.LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.venues, key = { it.code }) { venue ->
                            androidx.compose.material3.FilterChip(
                                selected = venue.code == state.selectedVenue,
                                onClick = { viewModel.selectVenue(venue.code) },
                                label = {
                                    Text(
                                        stringResource(R.string.tbf_venue_event_count, venue.name, venue.eventCount),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                if (state.isLoadingCard) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    val events = state.eventCard?.events ?: emptyList()
                    val venueCode = state.selectedVenue ?: ""
                    if (events.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                stringResource(R.string.tbf_no_events),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(events.size, key = { it }) { index ->
                                TbfRaceCard(
                                    competition = events[index],
                                    isResults = state.viewMode == com.horsegallop.feature.tbf.presentation.TbfViewMode.RESULTS,
                                    onClick = { onEventClick(venueCode, index) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TbfRaceCard(
    competition: com.horsegallop.domain.tbf.model.TbfCompetition,
    isResults: Boolean,
    onClick: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = semantic.cardElevated,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.tbf_competition_label, competition.no),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (competition.time.isNotBlank()) {
                        Text(
                            text = competition.time,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.tbf_event_distance, competition.distance, competition.surface),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val prize = competition.prize
                Text(
                    text = when {
                        prize >= 1_000_000 -> "₺${"%.1f".format(prize / 1_000_000.0)}M"
                        prize >= 1_000 -> "₺${"%.1f".format(prize / 1_000.0)}K"
                        else -> "₺$prize"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            competition.athletes.take(3).forEach { athlete ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.foundation.layout.Box(modifier = Modifier.width(20.dp)) {
                        Text(
                            text = athlete.no,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = athlete.name,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    if (isResults && athlete.result.isNotBlank()) {
                        Text(
                            text = athlete.result,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (athlete.time.isNotBlank()) {
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = athlete.time,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = athlete.odds,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EquestrianAgendaScreenPreview() {
    MaterialTheme {
        SyncStatusCard(
            syncStatus = null,
            sourceHealth = emptyList(),
            isLoading = false,
            isLoadingHealth = false,
            error = null,
            sourceHealthError = null,
            isTriggering = false,
            actionMessage = null,
            onTriggerSync = {},
            onTriggerDebugSync = {}
        )
    }
}

private fun String.toAgendaSyncLabel(): String {
    return runCatching {
        val formatter = DateTimeFormatter.ofPattern("d MMM HH:mm", Locale("tr"))
        Instant.parse(this)
            .atZone(ZoneId.of("Europe/Istanbul"))
            .format(formatter)
    }.getOrDefault(this)
}

@Composable
private fun AnnouncementCard(item: EquestrianAnnouncement, onOpen: () -> Unit) {
    FeedCard(
        title = item.title,
        subtitle = item.summary.ifBlank { item.publishedAtLabel },
        meta = item.publishedAtLabel,
        icon = Icons.Filled.Campaign,
        imageUrl = item.imageUrl,
        chipLabel = stringResource(R.string.equestrian_agenda_chip_announcements),
        onOpen = onOpen
    )
}

@Composable
private fun CompetitionCard(item: EquestrianCompetition, onOpen: () -> Unit) {
    FeedCard(
        title = item.title,
        subtitle = item.location.ifBlank { item.dateLabel },
        meta = item.dateLabel,
        icon = Icons.Filled.EmojiEvents,
        imageUrl = null,
        chipLabel = stringResource(R.string.equestrian_agenda_chip_competitions),
        onOpen = onOpen
    )
}

@Composable
private fun FeedCard(
    title: String,
    subtitle: String,
    meta: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    imageUrl: String?,
    chipLabel: String,
    onOpen: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                semantic.panelOverlay
                            )
                        )
                    )
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(semantic.imageOverlayStrong.copy(alpha = 0f), semantic.imageOverlayStrong)
                                )
                            )
                    )
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp),
                    color = semantic.panelOverlay.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = chipLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (meta.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = meta,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
