@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.horsegallop.feature.equestrian.presentation

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.horsegallop.R
import com.horsegallop.core.components.CalendarGrid
import com.horsegallop.domain.equestrian.model.TbfActivity
import com.horsegallop.domain.equestrian.model.TbfActivityType
import com.horsegallop.domain.equestrian.model.TbfDiscipline
import com.horsegallop.domain.equestrian.model.turkishName
import com.horsegallop.ui.theme.AppTheme
import com.horsegallop.ui.theme.LocalSemanticColors
import com.horsegallop.ui.theme.SemanticColors
import java.time.LocalDate

@Composable
fun TbfActivityScreen(
    onNavigateBack: () -> Unit,
    viewModel: TbfActivityViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TbfActivityContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onDayClick = viewModel::selectDay,
        onToggleFilter = viewModel::toggleDisciplineFilter,
        onClearAllFilters = viewModel::clearAllFilters
    )
}

@Composable
private fun TbfActivityContent(
    state: TbfActivityUiState,
    onNavigateBack: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (LocalDate) -> Unit,
    onToggleFilter: (TbfDiscipline) -> Unit,
    onClearAllFilters: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    val calendarTitle = stringResource(R.string.tbf_activity_calendar)
    val backCd = stringResource(R.string.back)

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = calendarTitle,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = backCd
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
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val daysMap = buildDaysMap(state)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item(key = "month_nav", contentType = "month_nav") {
                    MonthNavigationRow(
                        monthLabel = "${state.currentMonth.month.turkishName()} ${state.currentMonth.year}",
                        onPrevious = onPreviousMonth,
                        onNext = onNextMonth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                item(key = "discipline_filters", contentType = "filter_row") {
                    DisciplineFilterRow(
                        selectedFilters = state.disciplineFilters,
                        onToggle = onToggleFilter,
                        onClearAll = onClearAllFilters,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                item(key = "calendar_grid", contentType = "calendar") {
                    CalendarGrid(
                        yearMonth = state.currentMonth,
                        selectedDay = state.selectedDay,
                        daysWithActivities = daysMap,
                        onDayClick = onDayClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                if (state.selectedDay != null) {
                    val dayActivities = state.filteredActivitiesForSelectedDay
                    if (dayActivities.isEmpty()) {
                        item(key = "no_events", contentType = "empty") {
                            Text(
                                text = stringResource(R.string.tbf_no_events_day),
                                style = MaterialTheme.typography.bodyMedium,
                                color = semantic.cardStroke,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = dayActivities,
                            key = { _, activity -> activity.id },
                            contentType = { _, _ -> "activity_card" }
                        ) { _, activity ->
                            TbfActivityCard(
                                activity = activity,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildDaysMap(state: TbfActivityUiState): Map<LocalDate, List<TbfDiscipline>> {
    return state.daysWithActivities
        .associateWith { date ->
            state.activitiesForMonth
                .filter { a ->
                    (state.disciplineFilters.isEmpty() || a.discipline in state.disciplineFilters) &&
                        !date.isBefore(a.startDate) && !date.isAfter(a.endDate)
                }
                .map { it.discipline }
        }
}

@Composable
private fun MonthNavigationRow(
    monthLabel: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prevCd = stringResource(R.string.tbf_previous_month_cd)
    val nextCd = stringResource(R.string.tbf_next_month_cd)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onPrevious,
            modifier = Modifier.semantics { contentDescription = prevCd }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = monthLabel,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        IconButton(
            onClick = onNext,
            modifier = Modifier.semantics { contentDescription = nextCd }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DisciplineFilterRow(
    selectedFilters: Set<TbfDiscipline>,
    onToggle: (TbfDiscipline) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val allLabel = stringResource(R.string.tbf_filter_all)

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "filter_all", contentType = "filter_chip") {
            FilterChip(
                selected = selectedFilters.isEmpty(),
                onClick = onClearAll,
                label = { Text(text = allLabel, style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = semantic.chipSelected,
                    containerColor = semantic.chipUnselected
                )
            )
        }
        items(
            items = TbfDiscipline.entries.filter { it != TbfDiscipline.OTHER },
            key = { it.name },
            contentType = { "filter_chip" }
        ) { discipline ->
            FilterChip(
                selected = discipline in selectedFilters,
                onClick = { onToggle(discipline) },
                label = {
                    Text(
                        text = discipline.displayNameTr,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = semantic.chipSelected,
                    containerColor = semantic.chipUnselected
                )
            )
        }
    }
}

@Composable
fun TbfActivityCard(
    activity: TbfActivity,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Sol renk bandı — disiplin rengi
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(80.dp)
                    .background(
                        color = disciplineAccentColor(activity.discipline, semantic),
                        shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Başlık
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                // Organizasyon + Şehir
                Text(
                    text = "${activity.organization} — ${activity.city}",
                    style = MaterialTheme.typography.bodySmall,
                    color = semantic.cardStroke
                )
                // Tarih aralığı
                Text(
                    text = activity.dateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = semantic.info
                )
                // Disiplin + Tip chip'leri
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = activity.discipline.displayNameTr,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = semantic.chipUnselected
                        )
                    )
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = activity.type.displayNameTr,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = semantic.chipUnselected
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun disciplineAccentColor(
    discipline: TbfDiscipline,
    semantic: SemanticColors = LocalSemanticColors.current
) = when (discipline) {
    TbfDiscipline.SHOW_JUMPING -> MaterialTheme.colorScheme.primary
    TbfDiscipline.ENDURANCE -> semantic.success
    TbfDiscipline.DRESSAGE -> MaterialTheme.colorScheme.secondary
    TbfDiscipline.PONY -> MaterialTheme.colorScheme.tertiary
    TbfDiscipline.VAULTING -> semantic.warning
    TbfDiscipline.EVENTING -> semantic.ratingStar
    TbfDiscipline.OTHER -> semantic.cardStroke
}

@Preview(showBackground = true, name = "TbfActivity Full Screen")
@Composable
fun TbfActivityContentPreview() {
    AppTheme {
        val fakeActivity = TbfActivity(
            id = "1",
            startDate = LocalDate.of(2026, 3, 19),
            endDate = LocalDate.of(2026, 3, 22),
            title = "ANTALYA ATLI SPOR KULÜBÜ ENGEL ATLAMA YARIŞMALARI",
            organization = "ABAK",
            city = "ANTALYA",
            discipline = TbfDiscipline.SHOW_JUMPING,
            type = TbfActivityType.INCENTIVE
        )
        val fakeState = TbfActivityUiState(
            isLoading = false,
            currentMonth = java.time.YearMonth.of(2026, 3),
            selectedDay = LocalDate.of(2026, 3, 19),
            activitiesForMonth = listOf(fakeActivity),
            activitiesForSelectedDay = listOf(fakeActivity),
            disciplineFilters = emptySet()
        )
        TbfActivityContent(
            state = fakeState,
            onNavigateBack = {},
            onPreviousMonth = {},
            onNextMonth = {},
            onDayClick = {},
            onToggleFilter = {},
            onClearAllFilters = {}
        )
    }
}

@Preview(showBackground = true, name = "TbfActivity Card")
@Composable
fun TbfActivityCardPreview() {
    AppTheme {
        val fakeActivity = TbfActivity(
            id = "1",
            startDate = LocalDate.of(2026, 3, 19),
            endDate = LocalDate.of(2026, 3, 22),
            title = "ANTALYA ATLI SPOR KULÜBÜ ENGEL ATLAMA YARIŞMALARI",
            organization = "ABAK",
            city = "ANTALYA",
            discipline = TbfDiscipline.SHOW_JUMPING,
            type = TbfActivityType.INCENTIVE
        )
        TbfActivityCard(activity = fakeActivity)
    }
}
