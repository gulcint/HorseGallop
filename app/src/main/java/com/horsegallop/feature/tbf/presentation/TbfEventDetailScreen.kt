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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.horsegallop.domain.tbf.model.TbfAthlete
import com.horsegallop.domain.tbf.model.TbfCompetition
import com.horsegallop.domain.tbf.model.TbfEventCard
import com.horsegallop.ui.theme.AppTheme
import com.horsegallop.ui.theme.LocalSemanticColors

@Composable
fun TbfEventDetailScreen(
    onBack: () -> Unit,
    viewModel: TbfEventDetailViewModel = hiltViewModel()
) {
    val state by viewModel.ui.collectAsState()
    TbfEventDetailContent(
        state = state,
        onBack = onBack
    )
}

@Composable
private fun TbfEventDetailContent(
    state: TbfEventDetailUiState,
    onBack: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    val competition = state.eventCard?.events?.getOrNull(state.selectedEventIndex)
    val isResults = state.eventCard?.type == "results"

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (competition != null)
                            stringResource(R.string.tbf_event_distance, competition.distance, competition.surface)
                        else
                            stringResource(R.string.tbf_events_title),
                        style = MaterialTheme.typography.titleMedium,
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = semantic.screenTopBar,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            competition == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.tbf_no_events),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                EventDetailBody(
                    competition = competition,
                    isResults = isResults,
                    innerPadding = innerPadding
                )
            }
        }
    }
}

@Composable
private fun EventDetailBody(
    competition: TbfCompetition,
    isResults: Boolean,
    innerPadding: PaddingValues
) {
    val semantic = LocalSemanticColors.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Event info header
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                color = semantic.cardElevated,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.tbf_competition_label, competition.no),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (competition.name.isNotBlank()) {
                            Text(
                                text = competition.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        if (competition.time.isNotBlank()) {
                            Text(
                                text = competition.time,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = stringResource(R.string.tbf_event_distance, competition.distance, competition.surface),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Column header
        item {
            AthleteTableHeader(isResults = isResults)
            HorizontalDivider(
                color = semantic.cardStroke,
                thickness = 0.5.dp
            )
        }

        // Athletes
        itemsIndexed(competition.athletes) { index, athlete ->
            AthleteDetailRow(athlete = athlete, isResults = isResults)
            if (index < competition.athletes.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 32.dp),
                    color = semantic.cardStroke,
                    thickness = 0.5.dp
                )
            }
        }
    }
}

@Composable
private fun AthleteTableHeader(isResults: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.tbf_col_no),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(24.dp)
        )
        Text(
            text = stringResource(R.string.tbf_col_horse),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.tbf_col_jockey),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = if (isResults) "Derece" else "Oran",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(50.dp)
        )
    }
}

@Composable
private fun AthleteDetailRow(
    athlete: TbfAthlete,
    isResults: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = athlete.no,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = athlete.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (athlete.last6.isNotBlank()) {
                Text(
                    text = athlete.last6,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(modifier = Modifier.width(80.dp)) {
            Text(
                text = athlete.jockey,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.tbf_weight_kg, athlete.weight),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isResults && athlete.result.isNotBlank()) {
            Column(
                modifier = Modifier.width(50.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = athlete.result,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (athlete.time.isNotBlank()) {
                    Text(
                        text = athlete.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Text(
                text = athlete.odds,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(50.dp)
            )
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private fun previewAthletes() = listOf(
    TbfAthlete(no = "1", name = "RÜZGAR", jockey = "A.Çelik", trainer = "M.Yılmaz", owner = "F.Demir", weight = 58, age = "4", last6 = "1-2-3-1-2-1", odds = "2.50", bestTime = "1:23.5", result = "1", time = "1:22.3"),
    TbfAthlete(no = "2", name = "KARABURUN", jockey = "B.Kaya", trainer = "S.Arslan", owner = "T.Öztürk", weight = 56, age = "5", last6 = "3-1-2-4-1-3", odds = "5.00", bestTime = "1:24.1", result = "2", time = "1:22.8"),
    TbfAthlete(no = "3", name = "YILDIZHAN", jockey = "C.Doğan", trainer = "R.Şahin", owner = "E.Koç", weight = 57, age = "4", last6 = "2-3-1-2-3-2", odds = "7.50", bestTime = "1:24.8", result = "3", time = "1:23.1"),
    TbfAthlete(no = "4", name = "SÜRPRIZ", jockey = "D.Yıldız", trainer = "K.Güneş", owner = "A.Çetin", weight = 55, age = "3", last6 = "4-4-3-5-4-4", odds = "12.00", bestTime = "1:25.2", result = "", time = "")
)

@Preview(showBackground = true, name = "TbfEventDetail - Program Modu")
@Composable
private fun TbfEventDetailProgramPreview() {
    AppTheme {
        TbfEventDetailContent(
            state = TbfEventDetailUiState(
                isLoading = false,
                eventCard = TbfEventCard(
                    venue = "Ankara",
                    date = "2026-03-15",
                    type = "program",
                    events = listOf(
                        TbfCompetition(
                            no = "3",
                            name = "Üçüncü Koşu",
                            distance = 1600,
                            surface = "Kum",
                            time = "14:20",
                            prize = 300_000,
                            athletes = previewAthletes()
                        )
                    )
                ),
                selectedEventIndex = 0
            ),
            onBack = {}
        )
    }
}

@Preview(showBackground = true, name = "TbfEventDetail - Sonuç Modu")
@Composable
private fun TbfEventDetailResultsPreview() {
    AppTheme {
        TbfEventDetailContent(
            state = TbfEventDetailUiState(
                isLoading = false,
                eventCard = TbfEventCard(
                    venue = "Ankara",
                    date = "2026-03-15",
                    type = "results",
                    events = listOf(
                        TbfCompetition(
                            no = "3",
                            name = "Üçüncü Koşu",
                            distance = 1600,
                            surface = "Kum",
                            time = "14:20",
                            prize = 300_000,
                            athletes = previewAthletes()
                        )
                    )
                ),
                selectedEventIndex = 0
            ),
            onBack = {}
        )
    }
}
