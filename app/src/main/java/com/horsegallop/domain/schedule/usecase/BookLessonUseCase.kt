package com.horsegallop.domain.schedule.usecase

import com.horsegallop.domain.schedule.model.Reservation
import com.horsegallop.domain.schedule.repository.ScheduleRepository
import javax.inject.Inject

class BookLessonUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(lessonId: String): Result<Reservation> =
        repository.bookLesson(lessonId)
}
