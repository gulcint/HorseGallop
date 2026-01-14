package com.horsegallop.data.schedule.repository

import com.horsegallop.domain.schedule.model.Lesson
import com.horsegallop.domain.schedule.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor() : ScheduleRepository {
    override fun getLessons(): Flow<List<Lesson>> = flow {
        // Mock data
        emit(
            listOf(
                Lesson("l1", "2025-10-01 10:00", "Beginner Ride", "Alice"),
                Lesson("l2", "2025-10-02 14:00", "Trail Basics", "Bob"),
                Lesson("l3", "2025-10-03 09:00", "Jumping 101", "Charlie")
            )
        )
    }
}
