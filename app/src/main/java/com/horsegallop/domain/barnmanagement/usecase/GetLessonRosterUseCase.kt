package com.horsegallop.domain.barnmanagement.usecase

import com.horsegallop.domain.barnmanagement.model.StudentRosterEntry
import com.horsegallop.domain.barnmanagement.repository.BarnManagementRepository
import javax.inject.Inject

class GetLessonRosterUseCase @Inject constructor(
    private val repository: BarnManagementRepository
) {
    suspend operator fun invoke(lessonId: String): Result<List<StudentRosterEntry>> =
        repository.getLessonRoster(lessonId)
}
