package com.horsegallop.data.schedule.repository

import com.horsegallop.data.remote.functions.AppFunctionsDataSource
import com.horsegallop.domain.schedule.model.Lesson
import com.horsegallop.domain.schedule.model.Reservation
import com.horsegallop.domain.schedule.model.ReservationStatus
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
                    price = dto.price,
                    spotsTotal = dto.spotsTotal,
                    spotsAvailable = dto.spotsAvailable,
                    isBookedByMe = dto.isBookedByMe
                )
            }
            emit(remote)
        } catch (_: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun bookLesson(lessonId: String): Result<Reservation> = runCatching {
        val dto = functionsDataSource.bookLesson(lessonId)
        dto.toDomain()
    }

    override suspend fun cancelReservation(reservationId: String): Result<Unit> = runCatching {
        functionsDataSource.cancelReservation(reservationId)
    }

    override fun getMyReservations(): Flow<List<Reservation>> = flow {
        try {
            val items = functionsDataSource.getMyReservations().map { it.toDomain() }
            emit(items)
        } catch (_: Exception) {
            emit(emptyList())
        }
    }

    private fun com.horsegallop.data.remote.dto.ReservationFunctionsDto.toDomain() = Reservation(
        id = id,
        lessonId = lessonId,
        lessonTitle = lessonTitle,
        lessonDate = lessonDate,
        instructorName = instructorName,
        status = when (status) {
            "confirmed" -> ReservationStatus.CONFIRMED
            "cancelled" -> ReservationStatus.CANCELLED
            "completed" -> ReservationStatus.COMPLETED
            else -> ReservationStatus.PENDING
        },
        createdAt = createdAt
    )
}
