@file:OptIn(ExperimentalMaterial3Api::class)

package com.horsegallop.feature.tjk.presentation

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
import com.horsegallop.domain.tjk.model.TjkHippodrome
import com.horsegallop.domain.tjk.model.TjkHorse
import com.horsegallop.domain.tjk.model.TjkRace
import com.horsegallop.domain.tjk.model.TjkRaceCard
import com.horsegallop.ui.theme.AppTheme
import com.horsegallop.ui.theme.LocalSemanticColors

@Composable
fun TjkScreen(
    onBack: () -> Unit,
    onRaceClick: (hippodromeCode: String, raceIndex: Int) -> Unit,
    viewModel: TjkViewModel = hiltViewModel()
) {
    val state by viewModel.ui.collectAsState()
    TjkScreenContent(
        state = state,
        onBack = onBack,
        onRaceClick = onRaceClick,
        onHippodromeSelected = viewModel::selectHippodrome,
        onModeSwitch = viewModel::switchMode
    )
}

@Composable
private fun TjkScreenContent(
    state: TjkUiState,
    onBack: () -> Unit,
    onRaceClick: (hippodromeCode: String, raceIndex: Int) -> Unit,
    onHippodromeSelected: (String) -> Unit,
    onModeSwitch: (TjkViewMode) -> Unit
) {
    val semantic = LocalSemanticColors.current

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tjk_races_title),
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
                stringResource(R.string.tjk_program_tab),
                stringResource(R.string.tjk_results_tab)
            )
            val selectedTabIndex = if (state.viewMode == TjkViewMode.PROGRAM) 0 else 1
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = semantic.screenTopBar,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            onModeSwitch(if (index == 0) TjkViewMode.PROGRAM else TjkViewMode.RESULTS)
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
                                text = stringResource(R.string.tjk_loading),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    // Hippodrome selector
                    if (state.hippodromes.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.hippodromes) { hippodrome ->
                                HippodromeChip(
                                    hippodrome = hippodrome,
                                    isSelected = hippodrome.code == state.selectedHippodrome,
                                    onClick = { onHippodromeSelected(hippodrome.code) }
                                )
                            }
                        }
                    }

                    // Race list
                    if (state.isLoadingCard) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        val races = state.raceCard?.races ?: emptyList()
                        if (races.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.tjk_no_races),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            val hippodromeCode = state.selectedHippodrome ?: ""
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(races.size) { index ->
                                    TjkRaceCard(
                                        race = races[index],
                                        isResults = state.viewMode == TjkViewMode.RESULTS,
                                        onClick = { onRaceClick(hippodromeCode, index) }
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
private fun HippodromeChip(
    hippodrome: TjkHippodrome,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = "${hippodrome.name} (${hippodrome.raceCount})",
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
private fun TjkRaceCard(
    race: TjkRace,
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
            // Header row: race no + time | distance/surface | prize
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${race.no}. Koşu",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (race.time.isNotBlank()) {
                        Text(
                            text = race.time,
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
                        text = stringResource(R.string.tjk_race_distance, race.distance, race.surface),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = formatPrize(race.prize),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Top horses
            race.horses.take(3).forEach { horse ->
                HorseRow(horse = horse, isResults = isResults)
            }
        }
    }
}

@Composable
private fun HorseRow(
    horse: TjkHorse,
    isResults: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = horse.no,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(20.dp)
        )
        Text(
            text = horse.name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (isResults && horse.result.isNotBlank()) {
            Text(
                text = horse.result,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(4.dp))
            if (horse.time.isNotBlank()) {
                Text(
                    text = horse.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Text(
                text = horse.odds,
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

private fun fakeHippodromes() = listOf(
    TjkHippodrome(code = "ANK", name = "Ankara", raceCount = 9, time = "13:00"),
    TjkHippodrome(code = "IST", name = "İstanbul", raceCount = 8, time = "14:00"),
    TjkHippodrome(code = "IZM", name = "İzmir", raceCount = 7, time = "15:00")
)

private fun fakeHorses() = listOf(
    TjkHorse(no = "1", name = "RÜZGAR", jockey = "A.Çelik", trainer = "M.Yılmaz", owner = "F.Demir", weight = 58, age = "4", last6 = "1-2-3-1-2-1", odds = "2.50", bestTime = "1:23.5"),
    TjkHorse(no = "2", name = "KARABURUN", jockey = "B.Kaya", trainer = "S.Arslan", owner = "T.Öztürk", weight = 56, age = "5", last6 = "3-1-2-4-1-3", odds = "5.00", bestTime = "1:24.1"),
    TjkHorse(no = "3", name = "YILDIZHAN", jockey = "C.Doğan", trainer = "R.Şahin", owner = "E.Koç", weight = 57, age = "4", last6 = "2-3-1-2-3-2", odds = "7.50", bestTime = "1:24.8")
)

private fun fakeRaces() = listOf(
    TjkRace(no = "1", name = "Birinci Koşu", distance = 1200, surface = "Kum", time = "13:00", prize = 150_000, horses = fakeHorses()),
    TjkRace(no = "2", name = "İkinci Koşu", distance = 1600, surface = "Çim", time = "13:40", prize = 250_000, horses = fakeHorses())
)

private fun fakeRaceCard() = TjkRaceCard(
    hippodrome = "Ankara",
    date = "2026-03-15",
    type = "program",
    races = fakeRaces(),
    weather = "Açık",
    trackCondition = "Sert"
)

@Preview(showBackground = true, name = "TjkScreen - Program Modu")
@Composable
private fun TjkScreenProgramPreview() {
    AppTheme {
        TjkScreenContent(
            state = TjkUiState(
                isLoading = false,
                hippodromes = fakeHippodromes(),
                selectedHippodrome = "ANK",
                raceCard = fakeRaceCard(),
                viewMode = TjkViewMode.PROGRAM
            ),
            onBack = {},
            onRaceClick = { _, _ -> },
            onHippodromeSelected = {},
            onModeSwitch = {}
        )
    }
}

@Preview(showBackground = true, name = "TjkScreen - Sonuç Modu")
@Composable
private fun TjkScreenResultsPreview() {
    AppTheme {
        val resultHorses = fakeHorses().map { it.copy(result = (fakeHorses().indexOf(it) + 1).toString(), time = "1:23.${fakeHorses().indexOf(it) + 1}") }
        val resultRaces = fakeRaces().map { it.copy(horses = resultHorses) }
        TjkScreenContent(
            state = TjkUiState(
                isLoading = false,
                hippodromes = fakeHippodromes(),
                selectedHippodrome = "ANK",
                raceCard = fakeRaceCard().copy(races = resultRaces),
                viewMode = TjkViewMode.RESULTS
            ),
            onBack = {},
            onRaceClick = { _, _ -> },
            onHippodromeSelected = {},
            onModeSwitch = {}
        )
    }
}
