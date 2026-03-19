package com.horsegallop.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.horsegallop.domain.equestrian.model.TbfDiscipline
import com.horsegallop.ui.theme.AppTheme
import com.horsegallop.ui.theme.LocalSemanticColors
import com.horsegallop.ui.theme.SemanticColors
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDay: LocalDate?,
    daysWithActivities: Map<LocalDate, List<TbfDiscipline>>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val firstDay = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    // Mon=1..Sun=7 → offset 0..6
    val firstDayOfWeek = firstDay.dayOfWeek.value - 1

    Column(modifier = modifier) {
        // Gün başlıkları
        Row(Modifier.fillMaxWidth()) {
            listOf("Pt", "Sa", "Ça", "Pe", "Cu", "Ct", "Pz").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = semantic.cardStroke
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        val totalCells = firstDayOfWeek + daysInMonth
        val rows = (totalCells + 6) / 7

        repeat(rows) { row ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - firstDayOfWeek + 1
                    val isValid = dayNumber in 1..daysInMonth
                    val date = if (isValid) yearMonth.atDay(dayNumber) else null
                    val isSelected = date == selectedDay
                    val disciplines = if (date != null) daysWithActivities[date] ?: emptyList() else emptyList()

                    CalendarDayCell(
                        dayNumber = if (isValid) dayNumber else null,
                        isSelected = isSelected,
                        isToday = date == LocalDate.now(),
                        activityDisciplines = disciplines,
                        onClick = { date?.let(onDayClick) },
                        modifier = Modifier.weight(1f),
                        semantic = semantic
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int?,
    isSelected: Boolean,
    isToday: Boolean,
    activityDisciplines: List<TbfDiscipline>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    semantic: SemanticColors
) {
    val bgColor: Color = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> semantic.screenBase.copy(alpha = 0f)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(enabled = dayNumber != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (dayNumber != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = dayNumber.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                )
                if (activityDisciplines.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        activityDisciplines.take(3).forEach { discipline ->
                            Box(
                                Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(disciplineColor(discipline, semantic))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun disciplineColor(discipline: TbfDiscipline, semantic: SemanticColors): Color =
    when (discipline) {
        TbfDiscipline.SHOW_JUMPING -> MaterialTheme.colorScheme.primary
        TbfDiscipline.ENDURANCE -> semantic.success
        TbfDiscipline.DRESSAGE -> MaterialTheme.colorScheme.secondary
        TbfDiscipline.PONY -> MaterialTheme.colorScheme.tertiary
        TbfDiscipline.VAULTING -> semantic.warning
        TbfDiscipline.EVENTING -> semantic.ratingStar
        TbfDiscipline.OTHER -> semantic.cardStroke
    }

@Preview(showBackground = true)
@Composable
private fun CalendarGridPreview() {
    AppTheme {
        CalendarGrid(
            yearMonth = YearMonth.of(2026, 3),
            selectedDay = LocalDate.of(2026, 3, 19),
            daysWithActivities = mapOf(
                LocalDate.of(2026, 3, 19) to listOf(TbfDiscipline.SHOW_JUMPING),
                LocalDate.of(2026, 3, 21) to listOf(TbfDiscipline.SHOW_JUMPING),
                LocalDate.of(2026, 3, 27) to listOf(TbfDiscipline.ENDURANCE)
            ),
            onDayClick = {}
        )
    }
}
