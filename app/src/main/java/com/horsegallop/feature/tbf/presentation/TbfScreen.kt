@file:OptIn(ExperimentalMaterial3Api::class)

package com.horsegallop.feature.tbf.presentation

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.R
import com.horsegallop.domain.tbf.model.TbfVenue
import com.horsegallop.domain.tbf.model.TbfAthlete
import com.horsegallop.domain.tbf.model.TbfCompetition
import com.horsegallop.domain.tbf.model.TbfEventCard
import com.horsegallop.ui.theme.AppTheme
import com.horsegallop.ui.theme.LocalSemanticColors

@Composable
fun TbfScreen(
    onBack: () -> Unit,
    onEventClick: (venueCode: String, eventIndex: Int) -> Unit,
    viewModel: TbfViewModel = hiltViewModel()
) {
    val state by viewModel.ui.collectAsState()
    TbfScreenContent(
        state = state,
        onBack = onBack,
        onEventClick = onEventClick,
        onVenueSelected = viewModel::selectVenue,
        onModeSwitch = viewModel::switchMode
    )
}

@Composable
private fun TbfScreenContent(
    state: TbfUiState,
    onBack: () -> Unit,
    onEventClick: (venueCode: String, eventIndex: Int) -> Unit,
    onVenueSelected: (String) -> Unit,
    onModeSwitch: (TbfViewMode) -> Unit
) {
    val semantic = LocalSemanticColors.current

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tbf_events_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    Icon(
                        imageVector = Icons.Filled.Sports,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = semantic.screenTopBar,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab row: Program | Sonuçlar
            val tabs = listOf(
                stringResource(R.string.tbf_program_tab),
                stringResource(R.string.tbf_results_tab)
            )
            val selectedTabIndex = if (state.viewMode == TbfViewMode.PROGRAM) 0 else 1
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = semantic.screenTopBar,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            onModeSwitch(if (index == 0) TbfViewMode.PROGRAM else TbfViewMode.RESULTS)
                        },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    )
                }
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = stringResource(R.string.tbf_loading),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    // Venue selector
                    if (state.venues.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.venues) { venue ->
                                VenueChip(
                                    venue = venue,
                                    isSelected = venue.code == state.selectedVenue,
                                    onClick = { onVenueSelected(venue.code) }
                                )
                            }
                        }
                    }

                    // Event list
                    if (state.isLoadingCard) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        val events = state.eventCard?.events ?: emptyList()
                        if (events.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.tbf_no_events),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            val venueCode = state.selectedVenue ?: ""
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(events.size) { index ->
                                    TbfCompetitionCard(
                                        competition = events[index],
                                        isResults = state.viewMode == TbfViewMode.RESULTS,
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
}

@Composable
private fun VenueChip(
    venue: TbfVenue,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = "${venue.name} (${venue.eventCount})",
                style = MaterialTheme.typography.labelMedium
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun TbfCompetitionCard(
    competition: TbfCompetition,
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
            // Header row: event no + time | distance/surface | prize
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${competition.no}. Koşu",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
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
                Text(
                    text = formatPrize(competition.prize),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Top athletes
            competition.athletes.take(3).forEach { athlete ->
                AthleteRow(athlete = athlete, isResults = isResults)
            }
        }
    }
}

@Composable
private fun AthleteRow(
    athlete: TbfAthlete,
    isResults: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = athlete.no,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(20.dp)
        )
        Text(
            text = athlete.name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (isResults && athlete.result.isNotBlank()) {
            Text(
                text = athlete.result,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(4.dp))
            if (athlete.time.isNotBlank()) {
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

private fun formatPrize(prize: Long): String {
    return when {
        prize >= 1_000_000 -> "₺${prize / 1_000_000}M"
        prize >= 1_000 -> "₺${prize / 1_000}K"
        else -> "₺$prize"
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private fun fakeVenues() = listOf(
    TbfVenue(code = "ANK", name = "Ankara", eventCount = 9, time = "13:00"),
    TbfVenue(code = "IST", name = "İstanbul", eventCount = 8, time = "14:00"),
    TbfVenue(code = "IZM", name = "İzmir", eventCount = 7, time = "15:00")
)

private fun fakeAthletes() = listOf(
    TbfAthlete(no = "1", name = "RÜZGAR", jockey = "A.Çelik", trainer = "M.Yılmaz", owner = "F.Demir", weight = 58, age = "4", last6 = "1-2-3-1-2-1", odds = "2.50", bestTime = "1:23.5"),
    TbfAthlete(no = "2", name = "KARABURUN", jockey = "B.Kaya", trainer = "S.Arslan", owner = "T.Öztürk", weight = 56, age = "5", last6 = "3-1-2-4-1-3", odds = "5.00", bestTime = "1:24.1"),
    TbfAthlete(no = "3", name = "YILDIZHAN", jockey = "C.Doğan", trainer = "R.Şahin", owner = "E.Koç", weight = 57, age = "4", last6 = "2-3-1-2-3-2", odds = "7.50", bestTime = "1:24.8")
)

private fun fakeCompetitions() = listOf(
    TbfCompetition(no = "1", name = "Birinci Koşu", distance = 1200, surface = "Kum", time = "13:00", prize = 150_000, athletes = fakeAthletes()),
    TbfCompetition(no = "2", name = "İkinci Koşu", distance = 1600, surface = "Çim", time = "13:40", prize = 250_000, athletes = fakeAthletes())
)

private fun fakeEventCard() = TbfEventCard(
    venue = "Ankara",
    date = "2026-03-15",
    type = "program",
    events = fakeCompetitions(),
    weather = "Açık",
    trackCondition = "Sert"
)

@Preview(showBackground = true, name = "TbfScreen - Program Modu")
@Composable
private fun TbfScreenProgramPreview() {
    AppTheme {
        TbfScreenContent(
            state = TbfUiState(
                isLoading = false,
                venues = fakeVenues(),
                selectedVenue = "ANK",
                eventCard = fakeEventCard(),
                viewMode = TbfViewMode.PROGRAM
            ),
            onBack = {},
            onEventClick = { _, _ -> },
            onVenueSelected = {},
            onModeSwitch = {}
        )
    }
}

@Preview(showBackground = true, name = "TbfScreen - Sonuç Modu")
@Composable
private fun TbfScreenResultsPreview() {
    AppTheme {
        val resultAthletes = fakeAthletes().map { it.copy(result = (fakeAthletes().indexOf(it) + 1).toString(), time = "1:23.${fakeAthletes().indexOf(it) + 1}") }
        val resultCompetitions = fakeCompetitions().map { it.copy(athletes = resultAthletes) }
        TbfScreenContent(
            state = TbfUiState(
                isLoading = false,
                venues = fakeVenues(),
                selectedVenue = "ANK",
                eventCard = fakeEventCard().copy(events = resultCompetitions),
                viewMode = TbfViewMode.RESULTS
            ),
            onBack = {},
            onEventClick = { _, _ -> },
            onVenueSelected = {},
            onModeSwitch = {}
        )
    }
}
