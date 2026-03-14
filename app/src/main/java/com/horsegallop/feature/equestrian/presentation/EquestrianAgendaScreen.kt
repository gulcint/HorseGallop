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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.horsegallop.R
import com.horsegallop.domain.equestrian.model.EquestrianAnnouncement
import com.horsegallop.domain.equestrian.model.EquestrianCompetition
import com.horsegallop.domain.equestrian.model.FederatedBarnSyncStatus
import com.horsegallop.ui.theme.LocalSemanticColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun EquestrianAgendaScreen(
    onBack: () -> Unit,
    viewModel: EquestrianAgendaViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current
    val semantic = LocalSemanticColors.current

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
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.refresh))
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
                isLoading = state.isLoadingSyncStatus,
                error = state.syncStatusError,
                isTriggering = state.isTriggeringSync,
                actionMessage = state.syncActionMessage,
                onTriggerSync = viewModel::triggerManualSync
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
                            AnnouncementCard(item = item, onOpen = {
                                if (item.detailUrl.isNotBlank()) uriHandler.openUri(item.detailUrl)
                            })
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
                            CompetitionCard(item = item, onOpen = {
                                if (item.detailUrl.isNotBlank()) uriHandler.openUri(item.detailUrl)
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncStatusCard(
    syncStatus: FederatedBarnSyncStatus?,
    isLoading: Boolean,
    error: String?,
    isTriggering: Boolean,
    actionMessage: SyncActionMessage?,
    onTriggerSync: () -> Unit
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
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
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
                                null -> R.string.equestrian_agenda_sync_refreshed
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
    when {
        loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        error != null -> Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(text = error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        isEmpty -> Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(text = emptyMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        else -> content()
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
                                    colors = listOf(Color.Transparent, semantic.imageOverlayStrong)
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
