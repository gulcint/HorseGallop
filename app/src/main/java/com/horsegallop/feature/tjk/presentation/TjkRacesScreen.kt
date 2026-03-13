@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.horsegallop.feature.tjk.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.R
import com.horsegallop.domain.tjk.model.TjkCity
import com.horsegallop.domain.tjk.model.TjkRace
import com.horsegallop.domain.tjk.model.TjkRaceDay
import com.horsegallop.domain.tjk.model.TjkRaceResult
import com.horsegallop.ui.theme.AppTheme
import com.horsegallop.ui.theme.LocalSemanticColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TjkRacesScreen(
    onBack: () -> Unit,
    viewModel: TjkRacesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    TjkRacesContent(
        state = state,
        onBack = onBack,
        onCitySelected = viewModel::onCitySelected,
        onDateSelected = viewModel::onDateSelected,
        onRaceExpanded = viewModel::onRaceExpanded,
        onRefresh = viewModel::refresh,
        onClearError = viewModel::clearError
    )
}

@Composable
private fun TjkRacesContent(
    state: TjkUiState,
    onBack: () -> Unit,
    onCitySelected: (TjkCity) -> Unit,
    onDateSelected: (String) -> Unit,
    onRaceExpanded: (Int) -> Unit,
    onRefresh: () -> Unit,
    onClearError: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tjk_title),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
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
            // ── City filter chips ───────────────────────────────────────────
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.cities) { city ->
                    FilterChip(
                        selected = state.selectedCity?.id == city.id,
                        onClick = { onCitySelected(city) },
                        label = { Text(city.name, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // ── Date selector ───────────────────────────────────────────────
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.tjk_date_label, state.selectedDate),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── Content ─────────────────────────────────────────────────────
            when {
                state.isLoadingCities || state.isLoadingRaces -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.tjk_error_load),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(onClick = { onClearError(); onRefresh() }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }

                state.raceDay != null && state.raceDay.races.isNotEmpty() -> {
                    RaceDayList(
                        raceDay = state.raceDay,
                        expandedRaceNo = state.expandedRaceNo,
                        onRaceExpanded = onRaceExpanded
                    )
                }

                state.raceDay != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.tjk_no_races),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // ── Date Picker Dialog ──────────────────────────────────────────────────
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = runCatching {
                SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(state.selectedDate)?.time
            }.getOrNull() ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatted = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(millis))
                        onDateSelected(formatted)
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun RaceDayList(
    raceDay: TjkRaceDay,
    expandedRaceNo: Int?,
    onRaceExpanded: (Int) -> Unit
) {
    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.tjk_results_header, raceDay.cityName, raceDay.date),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        items(raceDay.races, key = { it.raceNo }) { race ->
            RaceCard(
                race = race,
                isExpanded = expandedRaceNo == race.raceNo,
                onToggle = { onRaceExpanded(race.raceNo) }
            )
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun RaceCard(
    race: TjkRace,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Column {
            // ── Race header (always visible) ─────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Race number badge
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "${race.raceNo}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = race.raceTitle.ifBlank { stringResource(R.string.tjk_race_number, race.raceNo) },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    val meta = listOfNotNull(
                        race.distance.takeIf { it.isNotBlank() },
                        race.surface.takeIf { it.isNotBlank() },
                        race.startTime.takeIf { it.isNotBlank() }
                    ).joinToString(" · ")
                    if (meta.isNotBlank()) {
                        Text(
                            text = meta,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (race.results.isNotEmpty()) {
                    IconButton(onClick = onToggle) {
                        Icon(
                            if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Results (expandable) ──────────────────────────────────────
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    HorizontalDivider(color = semantic.cardStroke)
                    // Header row
                    ResultHeaderRow()
                    race.results.forEachIndexed { idx, result ->
                        ResultRow(result = result, isTop3 = idx < 3)
                        if (idx < race.results.lastIndex) {
                            HorizontalDivider(
                                color = semantic.cardStroke.copy(alpha = 0.4f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ResultHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.tjk_col_pos),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp)
        )
        Text(
            text = stringResource(R.string.tjk_col_horse),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.tjk_col_jockey),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.tjk_col_time),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(48.dp)
        )
    }
}

@Composable
private fun ResultRow(result: TjkRaceResult, isTop3: Boolean) {
    val semantic = LocalSemanticColors.current
    val positionColor = when {
        result.position == "1" -> semantic.ratingStar
        isTop3 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = result.position,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isTop3) FontWeight.Bold else FontWeight.Normal,
            color = positionColor,
            modifier = Modifier.width(32.dp)
        )
        Text(
            text = result.horseName,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (result.position == "1") FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = result.jockey,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = result.time,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(48.dp)
        )
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun TjkRacesScreenPreview() {
    AppTheme {
        TjkRacesContent(
            state = TjkUiState(
                isLoadingCities = false,
                isLoadingRaces = false,
                cities = listOf(
                    TjkCity(3, "İstanbul"),
                    TjkCity(2, "İzmir"),
                    TjkCity(5, "Ankara")
                ),
                selectedCity = TjkCity(3, "İstanbul"),
                selectedDate = "13/03/2026",
                raceDay = TjkRaceDay(
                    date = "13/03/2026",
                    cityId = 3,
                    cityName = "İstanbul",
                    races = listOf(
                        TjkRace(
                            raceNo = 1,
                            raceTitle = "Maiden 3 yaşlı",
                            distance = "1400 m",
                            surface = "Kum",
                            startTime = "13:00",
                            results = listOf(
                                TjkRaceResult("1", "KARAELMAS", "A. Şahin", "M. Ergün", "56", "1:28.40"),
                                TjkRaceResult("2", "DUMAN", "C. Yıldız", "H. Kaya", "54", "1:28.65"),
                                TjkRaceResult("3", "FIRTINA", "T. Akın", "S. Demir", "55", "1:28.90")
                            )
                        ),
                        TjkRace(
                            raceNo = 2,
                            raceTitle = "Altılı Ganyan",
                            distance = "1800 m",
                            surface = "Çim",
                            startTime = "13:35",
                            results = emptyList()
                        )
                    )
                ),
                expandedRaceNo = 1
            ),
            onBack = {},
            onCitySelected = {},
            onDateSelected = {},
            onRaceExpanded = {},
            onRefresh = {},
            onClearError = {}
        )
    }
}
