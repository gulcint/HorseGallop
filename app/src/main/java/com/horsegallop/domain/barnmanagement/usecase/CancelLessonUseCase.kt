package com.horsegallop.domain.barnmanagement.usecase

import com.horsegallop.domain.barnmanagement.repository.BarnManagementRepository
import javax.inject.Inject

class CancelLessonUseCase @Inject constructor(
    private val repository: BarnManagementRepository
) {
    suspend operator fun invoke(lessonId: String): Result<Unit> =
        repository.cancelLesson(lessonId)
}
