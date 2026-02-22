package com.horsegallop.domain.schedule.repository

import com.horsegallop.domain.schedule.model.Lesson
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun getLessons(): Flow<List<Lesson>>
}
