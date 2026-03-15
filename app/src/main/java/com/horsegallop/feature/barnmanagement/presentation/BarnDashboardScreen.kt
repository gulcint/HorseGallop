@file:OptIn(ExperimentalMaterial3Api::class)

package com.horsegallop.feature.barnmanagement.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.horsegallop.R
import com.horsegallop.domain.barnmanagement.model.BarnStats
import com.horsegallop.domain.barnmanagement.model.ManagedLesson
import com.horsegallop.ui.theme.LocalSemanticColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BarnDashboardScreen(
    onBack: () -> Unit,
    onCreateLesson: (barnId: String) -> Unit,
    onViewRoster: (lessonId: String) -> Unit,
    viewModel: BarnDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.ui.collectAsStateWithLifecycle()

    BarnDashboardContent(
        uiState = uiState,
        onBack = onBack,
        onCreateLesson = {
            // barnId comes from ViewModel's savedStateHandle, pass via nav
            onCreateLesson("")
        },
        onViewRoster = onViewRoster,
        onCancelLesson = viewModel::cancelLesson,
        onRetry = viewModel::loadData
    )
}

@Composable
fun BarnDashboardContent(
    uiState: BarnDashboardUiState,
    onBack: () -> Unit,
    onCreateLesson: () -> Unit,
    onViewRoster: (lessonId: String) -> Unit,
    onCancelLesson: (lessonId: String) -> Unit,
    onRetry: () -> Unit
) {
    val semantic = LocalSemanticColors.current

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.barn_management_title),
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateLesson,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.barn_create_lesson_title)
                )
            }
        }
    ) { innerPadding ->
        when {
            uiState.loading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            uiState.error != null -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = uiState.error,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(onClick = onRetry) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.stats?.let { stats ->
                    item {
                        BarnStatsRow(stats = stats)
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.barn_upcoming_lessons),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                }

                val upcomingLessons = uiState.lessons.filter { !it.isCancelled }
                if (upcomingLessons.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = semantic.cardSubtle
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.barn_no_upcoming_lessons),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(upcomingLessons, key = { it.id }) { lesson ->
                        ManagedLessonCard(
                            lesson = lesson,
                            isCancelling = uiState.cancellingLessonId == lesson.id,
                            onViewRoster = { onViewRoster(lesson.id) },
                            onCancel = { onCancelLesson(lesson.id) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun BarnStatsRow(stats: BarnStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BarnStatCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.barn_stats_total_lessons),
            value = stats.totalLessons.toString()
        )
        BarnStatCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.barn_stats_reservations),
            value = stats.totalReservations.toString()
        )
        BarnStatCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.barn_stats_students),
            value = stats.uniqueStudents.toString()
        )
    }
}

@Composable
fun BarnStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    val semantic = LocalSemanticColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = semantic.cardElevated,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ManagedLessonCard(
    lesson: ManagedLesson,
    isCancelling: Boolean,
    onViewRoster: () -> Unit,
    onCancel: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val dateLabel = remember(lesson.startTimeMs) {
        dateFormat.format(Date(lesson.startTimeMs))
    }
    val spotsRemaining = lesson.spotsTotal - lesson.spotsBooked

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = semantic.cardElevated,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = lesson.instructorName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (spotsRemaining > 0) semantic.success.copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = "${lesson.spotsBooked}/${lesson.spotsTotal}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (spotsRemaining > 0) semantic.success
                        else MaterialTheme.colorScheme.error
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 1.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onViewRoster,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.barn_lesson_roster_title),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                if (isCancelling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    TextButton(
                        onClick = onCancel,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.barn_lesson_cancel),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BarnDashboardPreview() {
    MaterialTheme {
        BarnDashboardContent(
            uiState = BarnDashboardUiState(
                loading = false,
                stats = BarnStats(
                    totalLessons = 24,
                    totalReservations = 87,
                    uniqueStudents = 15,
                    upcomingLessonsCount = 3
                ),
                lessons = listOf(
                    ManagedLesson(
                        id = "1",
                        title = "Beginner Dressage",
                        instructorName = "Ahmet Yılmaz",
                        startTimeMs = System.currentTimeMillis() + 86_400_000L,
                        durationMin = 60,
                        level = "Beginner",
                        price = 250.0,
                        spotsTotal = 8,
                        spotsBooked = 5,
                        barnId = "barn1"
                    ),
                    ManagedLesson(
                        id = "2",
                        title = "Advanced Jumping",
                        instructorName = "Mehmet Kaya",
                        startTimeMs = System.currentTimeMillis() + 172_800_000L,
                        durationMin = 90,
                        level = "Advanced",
                        price = 400.0,
                        spotsTotal = 6,
                        spotsBooked = 6,
                        barnId = "barn1"
                    )
                )
            ),
            onBack = {},
            onCreateLesson = {},
            onViewRoster = {},
            onCancelLesson = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BarnDashboardDarkPreview() {
    MaterialTheme {
        BarnDashboardContent(
            uiState = BarnDashboardUiState(
                loading = false,
                stats = BarnStats(12, 45, 8, 2),
                lessons = emptyList()
            ),
            onBack = {},
            onCreateLesson = {},
            onViewRoster = {},
            onCancelLesson = {},
            onRetry = {}
        )
    }
}
