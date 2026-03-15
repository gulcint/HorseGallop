package com.horsegallop.data.barnmanagement.repository

import com.horsegallop.data.remote.functions.AppFunctionsDataSource
import com.horsegallop.domain.barnmanagement.model.BarnStats
import com.horsegallop.domain.barnmanagement.model.ManagedLesson
import com.horsegallop.domain.barnmanagement.model.StudentRosterEntry
import com.horsegallop.domain.barnmanagement.repository.BarnManagementRepository
import com.horsegallop.domain.barnmanagement.repository.CreateLessonRequest
import javax.inject.Inject

class BarnManagementRepositoryImpl @Inject constructor(
    private val functionsDataSource: AppFunctionsDataSource
) : BarnManagementRepository {

    override suspend fun getBarnStats(barnId: String): Result<BarnStats> = runCatching {
        val dto = functionsDataSource.getBarnStats(barnId)
        BarnStats(
            totalLessons = dto.totalLessons,
            totalReservations = dto.totalReservations,
            uniqueStudents = dto.uniqueStudents,
            upcomingLessonsCount = dto.upcomingLessonsCount
        )
    }

    override suspend fun getManagedLessons(barnId: String): Result<List<ManagedLesson>> = runCatching {
        functionsDataSource.getManagedLessons(barnId).map { dto ->
            ManagedLesson(
                id = dto.id,
                title = dto.title,
                instructorName = dto.instructorName,
                startTimeMs = dto.startTimeMs,
                durationMin = dto.durationMin,
                level = dto.level,
                price = dto.price,
                spotsTotal = dto.spotsTotal,
                spotsBooked = dto.spotsBooked,
                barnId = dto.barnId,
                isCancelled = dto.isCancelled
            )
        }
    }

    override suspend fun createLesson(lesson: CreateLessonRequest): Result<ManagedLesson> = runCatching {
        val dto = functionsDataSource.createLesson(
            barnId = lesson.barnId,
            title = lesson.title,
            instructorName = lesson.instructorName,
            startTimeMs = lesson.startTimeMs,
            durationMin = lesson.durationMin,
            level = lesson.level,
            price = lesson.price,
            spotsTotal = lesson.spotsTotal
        )
        ManagedLesson(
            id = dto.id,
            title = dto.title,
            instructorName = dto.instructorName,
            startTimeMs = dto.startTimeMs,
            durationMin = dto.durationMin,
            level = dto.level,
            price = dto.price,
            spotsTotal = dto.spotsTotal,
            spotsBooked = dto.spotsBooked,
            barnId = dto.barnId,
            isCancelled = dto.isCancelled
        )
    }

    override suspend fun cancelLesson(lessonId: String): Result<Unit> = runCatching {
        functionsDataSource.cancelLesson(lessonId)
    }

    override suspend fun getLessonRoster(lessonId: String): Result<List<StudentRosterEntry>> = runCatching {
        functionsDataSource.getLessonRoster(lessonId).map { dto ->
            StudentRosterEntry(
                userId = dto.userId,
                displayName = dto.displayName,
                email = dto.email,
                reservationId = dto.reservationId,
                bookedAtMs = dto.bookedAtMs
            )
        }
    }
}
