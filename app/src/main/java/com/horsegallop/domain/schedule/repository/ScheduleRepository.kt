package com.horsegallop.domain.schedule.repository

import com.horsegallop.domain.schedule.model.Lesson
import com.horsegallop.domain.schedule.model.Reservation
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun getLessons(): Flow<List<Lesson>>
    suspend fun bookLesson(lessonId: String): Result<Reservation>
    suspend fun cancelReservation(reservationId: String): Result<Unit>
    fun getMyReservations(): Flow<List<Reservation>>
}
