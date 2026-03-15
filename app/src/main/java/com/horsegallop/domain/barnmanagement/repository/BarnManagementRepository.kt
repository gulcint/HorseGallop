package com.horsegallop.domain.barnmanagement.repository

import com.horsegallop.domain.barnmanagement.model.BarnStats
import com.horsegallop.domain.barnmanagement.model.ManagedLesson
import com.horsegallop.domain.barnmanagement.model.StudentRosterEntry

interface BarnManagementRepository {
    suspend fun getBarnStats(barnId: String): Result<BarnStats>
    suspend fun getManagedLessons(barnId: String): Result<List<ManagedLesson>>
    suspend fun createLesson(lesson: CreateLessonRequest): Result<ManagedLesson>
    suspend fun cancelLesson(lessonId: String): Result<Unit>
    suspend fun getLessonRoster(lessonId: String): Result<List<StudentRosterEntry>>
}

data class CreateLessonRequest(
    val barnId: String,
    val title: String,
    val instructorName: String,
    val startTimeMs: Long,
    val durationMin: Int,
    val level: String,
    val price: Double,
    val spotsTotal: Int
)
