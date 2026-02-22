package com.horsegallop.data.schedule.repository

import com.horsegallop.data.remote.ApiService
import com.horsegallop.domain.schedule.model.Lesson
import com.horsegallop.domain.schedule.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ScheduleRepository {
    override fun getLessons(): Flow<List<Lesson>> = flow {
        try {
            val remote = apiService.getLessonsV2().map { dto ->
                Lesson(
                    id = dto.id,
                    date = dto.date,
                    title = dto.title,
                    instructorName = dto.instructorName,
                    durationMin = dto.durationMin ?: 0,
                    level = dto.level ?: "",
                    price = dto.price ?: 0.0
                )
            }
            emit(remote)
        } catch (_: Exception) {
            emit(
                listOf(
                    Lesson("l1", "2025-10-01 10:00", "Beginner Ride", "Alice"),
                    Lesson("l2", "2025-10-02 14:00", "Trail Basics", "Bob"),
                    Lesson("l3", "2025-10-03 09:00", "Jumping 101", "Charlie")
                )
            )
        }
    }
}
