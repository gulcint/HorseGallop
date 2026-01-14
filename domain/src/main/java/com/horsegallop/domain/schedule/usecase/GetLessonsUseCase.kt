package com.horsegallop.domain.schedule.usecase

import com.horsegallop.domain.schedule.model.Lesson
import com.horsegallop.domain.schedule.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLessonsUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    operator fun invoke(): Flow<List<Lesson>> {
        return scheduleRepository.getLessons()
    }
}
