package com.horsegallop.data.schedule.repository

import com.horsegallop.data.remote.functions.AppFunctionsDataSource
import com.horsegallop.domain.schedule.model.Lesson
import com.horsegallop.domain.schedule.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val functionsDataSource: AppFunctionsDataSource
) : ScheduleRepository {
    override fun getLessons(): Flow<List<Lesson>> = flow {
        try {
            val remote = functionsDataSource.getLessons().map { dto ->
                Lesson(
                    id = dto.id,
                    date = dto.date,
                    title = dto.title,
                    instructorName = dto.instructorName,
                    durationMin = dto.durationMin,
                    level = dto.level,
                    price = dto.price
                )
            }
            emit(remote)
        } catch (_: Exception) {
            emit(emptyList())
        }
    }
}
