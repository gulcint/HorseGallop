package com.horsegallop.feature.schedule.presentation

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.horsegallop.core.R

data class LessonUi(
  val id: String,
  val date: String,
  val title: String,
  val instructorName: String
)

@Composable
fun ScheduleScreen(
  lessons: List<LessonUi>,
  onLessonClick: (String) -> Unit
) {
  LazyColumn {
    items(lessons) { lesson ->
      ListItem(
        headlineContent = { Text(text = lesson.title) },
        supportingContent = { 
          Text(text = "${lesson.date} • ${stringResource(R.string.instructor_name)}: ${lesson.instructorName}") 
        },
        overlineContent = { Text(text = lesson.id) }
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
        LessonUi("l1", "2025-10-01 10:00", "Beginner Ride", "Alice"),
        LessonUi("l2", "2025-10-02 14:00", "Trail Basics", "Bob")
      ),
      onLessonClick = {}
    )
  }
}
