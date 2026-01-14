package com.horsegallop.feature.schedule.presentation

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.core.R
import com.horsegallop.domain.schedule.model.Lesson

@Composable
fun ScheduleRoute(
    viewModel: ScheduleViewModel = hiltViewModel(),
    onLessonClick: (String) -> Unit
) {
    val lessons by viewModel.lessons.collectAsState()
    ScheduleScreen(
        lessons = lessons,
        onLessonClick = onLessonClick
    )
}

@Composable
fun ScheduleScreen(
    lessons: List<Lesson>,
    onLessonClick: (String) -> Unit
) {
    LazyColumn {
        items(lessons) { lesson ->
            ListItem(
                headlineContent = { Text(text = lesson.title, style = MaterialTheme.typography.titleMedium) },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.lesson_details, lesson.date, lesson.instructorName),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                overlineContent = { Text(text = lesson.id, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}

@Preview
@Composable
private fun SchedulePreview() {
    MaterialTheme {
        ScheduleScreen(
            lessons = listOf(
                Lesson("l1", "2025-10-01 10:00", "Beginner Ride", "Alice"),
                Lesson("l2", "2025-10-02 14:00", "Trail Basics", "Bob")
            ),
            onLessonClick = {}
        )
    }
}
