package com.horsegallop.data.barnmanagement.repository

import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.data.remote.supabase.SupabaseManagedLessonDto
import com.horsegallop.domain.barnmanagement.model.BarnStats
import com.horsegallop.domain.barnmanagement.model.ManagedLesson
import com.horsegallop.domain.barnmanagement.model.StudentRosterEntry
import com.horsegallop.domain.barnmanagement.repository.BarnManagementRepository
import com.horsegallop.domain.barnmanagement.repository.CreateLessonRequest
import javax.inject.Inject

class BarnManagementRepositoryImpl @Inject constructor(
    private val supabaseDataSource: SupabaseDataSource
) : BarnManagementRepository {

    override suspend fun getBarnStats(barnId: String): Result<BarnStats> = runCatching {
        val statsMap = supabaseDataSource.getBarnStats(barnId)
        BarnStats(
            totalLessons = (statsMap["totalLessons"] as? Int) ?: 0,
            totalReservations = (statsMap["totalReservations"] as? Int) ?: 0,
            uniqueStudents = (statsMap["uniqueStudents"] as? Int) ?: 0,
            upcomingLessonsCount = (statsMap["upcomingLessonsCount"] as? Int) ?: 0
        )
    }

    override suspend fun getManagedLessons(barnId: String): Result<List<ManagedLesson>> = runCatching {
        supabaseDataSource.getManagedLessons(barnId).map { dto ->
            dto.toDomain()
        }
    }

    override suspend fun createLesson(lesson: CreateLessonRequest): Result<ManagedLesson> = runCatching {
        val dto = SupabaseManagedLessonDto(
            id = "",
            barnId = lesson.barnId,
            title = lesson.title,
            instructorName = lesson.instructorName,
            startTimeMs = lesson.startTimeMs,
            durationMin = lesson.durationMin,
            level = lesson.level,
            price = lesson.price,
            spotsTotal = lesson.spotsTotal
        )
        supabaseDataSource.createLesson(dto).toDomain()
    }

    override suspend fun cancelLesson(lessonId: String): Result<Unit> = runCatching {
        supabaseDataSource.cancelLesson(lessonId)
    }

    override suspend fun getLessonRoster(lessonId: String): Result<List<StudentRosterEntry>> = runCatching {
        supabaseDataSource.getLessonRoster(lessonId).map { dto ->
            StudentRosterEntry(
                userId = dto.userId,
                displayName = dto.displayName,
                email = dto.email,
                reservationId = dto.reservationId,
                bookedAtMs = dto.bookedAtMs
            )
        }
    }

    private fun SupabaseManagedLessonDto.toDomain() = ManagedLesson(
        id = id,
        title = title,
        instructorName = instructorName,
        startTimeMs = startTimeMs,
        durationMin = durationMin,
        level = level,
        price = price,
        spotsTotal = spotsTotal,
        spotsBooked = spotsBooked,
        barnId = barnId,
        isCancelled = isCancelled
    )
}
