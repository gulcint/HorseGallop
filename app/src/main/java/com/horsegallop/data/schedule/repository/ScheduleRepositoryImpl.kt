package com.horsegallop.data.schedule.repository

import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.data.remote.supabase.SupabaseReservationDto
import com.horsegallop.domain.schedule.model.Lesson
import com.horsegallop.domain.schedule.model.Reservation
import com.horsegallop.domain.schedule.model.ReservationStatus
import com.horsegallop.domain.schedule.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val supabaseDataSource: SupabaseDataSource
) : ScheduleRepository {

    override fun getLessons(): Flow<List<Lesson>> = flow {
        emit(supabaseDataSource.getLessons().map { dto ->
            Lesson(
                id = dto.id,
                date = dto.lessonDate ?: "",
                title = dto.title,
                instructorName = dto.instructorName,
                durationMin = dto.durationMin,
                level = dto.level,
                price = dto.price,
                spotsTotal = dto.spotsTotal,
                spotsAvailable = dto.spotsAvailable,
                isBookedByMe = false
            )
        })
    }.catch { emit(emptyList()) }

    override suspend fun bookLesson(lessonId: String): Result<Reservation> = runCatching {
        // Find the lesson first, then book it
        val lessons = supabaseDataSource.getLessons()
        val lesson = lessons.find { it.id == lessonId }
            ?: error("Lesson $lessonId not found")
        val reservation = supabaseDataSource.bookLesson(lesson)
        reservation.toDomain()
    }

    override suspend fun cancelReservation(reservationId: String): Result<Unit> = runCatching {
        supabaseDataSource.cancelReservation(reservationId)
    }

    override fun getMyReservations(): Flow<List<Reservation>> = flow {
        emit(supabaseDataSource.getMyReservations().map { it.toDomain() })
    }.catch { emit(emptyList()) }

    private fun SupabaseReservationDto.toDomain() = Reservation(
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
