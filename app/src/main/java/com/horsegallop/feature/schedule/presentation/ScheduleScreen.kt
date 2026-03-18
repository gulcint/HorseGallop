package com.horsegallop.feature.schedule.presentation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.horsegallop.domain.schedule.model.Lesson
import com.horsegallop.ui.theme.LocalSemanticColors

enum class ScheduleTab { BROWSE, MY_RESERVATIONS }

@Composable
fun ScheduleRoute(
    viewModel: ScheduleViewModel = hiltViewModel(),
    onMyReservations: () -> Unit = {},
    onWriteReview: (lessonId: String, lessonTitle: String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(ScheduleTab.BROWSE) }

    ScheduleScreen(
        uiState = uiState,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        onRetry = { viewModel.refresh() },
        onBookLesson = { lessonId -> viewModel.bookLesson(lessonId) },
        onClearBookingState = { viewModel.clearBookingState() },
        onMyReservations = onMyReservations,
        onWriteReview = onWriteReview,
        onCancelReservation = { reservationId -> viewModel.cancelReservation(reservationId) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    uiState: ScheduleUiState,
    selectedTab: ScheduleTab = ScheduleTab.BROWSE,
    onTabSelected: (ScheduleTab) -> Unit = {},
    onRetry: () -> Unit = {},
    onBookLesson: (String) -> Unit = {},
    onClearBookingState: () -> Unit = {},
    onMyReservations: () -> Unit = {},
    onWriteReview: (lessonId: String, lessonTitle: String) -> Unit = { _, _ -> },
    onCancelReservation: (reservationId: String) -> Unit = {}
) {
    val semantic = LocalSemanticColors.current
    var selectedLesson by remember { mutableStateOf<Lesson?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    val bookingSuccessMsg = stringResource(R.string.booking_success)
    LaunchedEffect(uiState.bookingSuccess) {
        if (uiState.bookingSuccess) {
            snackbarHostState.showSnackbar(
                message = bookingSuccessMsg,
                duration = SnackbarDuration.Short
            )
            selectedLesson = null
            onClearBookingState()
        }
    }

    LaunchedEffect(uiState.bookingError) {
        uiState.bookingError?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            onClearBookingState()
        }
    }

    val tabBrowseLabel = stringResource(R.string.schedule_tab_browse)
    val tabReservationsLabel = stringResource(R.string.schedule_tab_reservations)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = semantic.screenBase
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                Tab(
                    selected = selectedTab == ScheduleTab.BROWSE,
                    onClick = { onTabSelected(ScheduleTab.BROWSE) },
                    text = { Text(tabBrowseLabel) }
                )
                Tab(
                    selected = selectedTab == ScheduleTab.MY_RESERVATIONS,
                    onClick = { onTabSelected(ScheduleTab.MY_RESERVATIONS) },
                    text = { Text(tabReservationsLabel) }
                )
            }

            when (selectedTab) {
                ScheduleTab.BROWSE -> {
                    BrowseTabContent(
                        uiState = uiState,
                        onRetry = onRetry,
                        onLessonClick = { lesson -> selectedLesson = lesson }
                    )
                }
                ScheduleTab.MY_RESERVATIONS -> {
                    MyReservationsContent(
                        uiState = uiState,
                        onWriteReview = onWriteReview,
                        onCancelReservation = onCancelReservation,
                        onBack = { onTabSelected(ScheduleTab.BROWSE) }
                    )
                }
            }
        }
    }

    selectedLesson?.let { lesson ->
        ModalBottomSheet(
            onDismissRequest = { selectedLesson = null },
            sheetState = sheetState,
            containerColor = semantic.panelOverlay
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = lesson.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(lesson.date, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                    Text(lesson.instructorName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (lesson.durationMin > 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.tertiary)
                        Text(stringResource(R.string.lesson_duration_min, lesson.durationMin), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (lesson.level.isNotBlank()) {
                    Text(stringResource(R.string.lesson_level_label, lesson.level), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (lesson.price > 0) {
                    Text("₺${lesson.price.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                }
                if (lesson.spotsTotal > 0) {
                    val spotsColor = if (lesson.isFull) MaterialTheme.colorScheme.error else semantic.success
                    Text(
                        text = if (lesson.isFull) stringResource(R.string.lesson_spots_full)
                               else stringResource(R.string.lesson_spots_available, lesson.spotsAvailable, lesson.spotsTotal),
                        style = MaterialTheme.typography.bodySmall,
                        color = spotsColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                HorizontalDivider(color = semantic.dividerSoft)

                when {
                    lesson.isBookedByMe -> {
                        Button(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = semantic.stateOverlaySuccess
                            )
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(8.dp))
                            Text(stringResource(R.string.lesson_booked_label), color = semantic.success)
                        }
                    }
                    lesson.isFull -> {
                        Button(onClick = {}, modifier = Modifier.fillMaxWidth(), enabled = false) {
                            Text(stringResource(R.string.lesson_full_label))
                        }
                    }
                    else -> {
                        Button(
                            onClick = { onBookLesson(lesson.id) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.bookingInProgress
                        ) {
                            if (uiState.bookingInProgress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(Modifier.size(8.dp))
                            }
                            Text(stringResource(R.string.book_lesson))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun BrowseTabContent(
    uiState: ScheduleUiState,
    onRetry: () -> Unit,
    onLessonClick: (Lesson) -> Unit
) {
    val semantic = LocalSemanticColors.current
    when {
        uiState.loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        }

        uiState.error != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = semantic.calloutErrorContainer),
                    border = BorderStroke(1.dp, semantic.calloutBorderError)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = uiState.error ?: stringResource(R.string.backend_error_generic),
                            color = semantic.calloutOnContainer
                        )
                        Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                    }
                }
            }
        }

        uiState.isEmpty -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.schedule_no_sessions_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.schedule_no_sessions_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    OutlinedButton(onClick = onRetry) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.lessons, key = { it.id }) { lesson ->
                    LessonCard(lesson = lesson, onClick = { onLessonClick(lesson) })
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScheduleScreenPreview() {
    MaterialTheme {
        ScheduleScreen(
            uiState = ScheduleUiState(
                loading = false,
                lessons = listOf(
                    Lesson(
                        id = "1",
                        date = "Cumartesi, 15 Mart 10:00",
                        title = "Temel Binicilik",
                        instructorName = "Ahmet Yılmaz",
                        durationMin = 60,
                        level = "Başlangıç",
                        price = 250.0,
                        spotsTotal = 8,
                        spotsAvailable = 3
                    ),
                    Lesson(
                        id = "2",
                        date = "Pazar, 16 Mart 14:00",
                        title = "İleri Atlama",
                        instructorName = "Zeynep Kaya",
                        durationMin = 90,
                        level = "İleri",
                        price = 400.0,
                        spotsTotal = 5,
                        spotsAvailable = 0
                    )
                )
            )
        )
    }
}

@Composable
private fun LessonCard(lesson: Lesson, onClick: () -> Unit) {
    val semantic = LocalSemanticColors.current
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = BorderStroke(
            1.dp,
            if (lesson.isBookedByMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            else semantic.cardStroke
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(lesson.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                when {
                    lesson.isBookedByMe -> Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                        Text(stringResource(R.string.lesson_booked_badge), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                    }
                    lesson.isFull -> Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
                        Text(stringResource(R.string.lesson_full_badge), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                Text(lesson.date, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                Text(lesson.instructorName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
