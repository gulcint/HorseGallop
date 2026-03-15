package com.horsegallop.domain.barnmanagement.usecase

import com.horsegallop.domain.barnmanagement.model.ManagedLesson
import com.horsegallop.domain.barnmanagement.repository.BarnManagementRepository
import com.horsegallop.domain.barnmanagement.repository.CreateLessonRequest
import javax.inject.Inject

class CreateLessonUseCase @Inject constructor(
    private val repository: BarnManagementRepository
) {
    suspend operator fun invoke(request: CreateLessonRequest): Result<ManagedLesson> =
        repository.createLesson(request)
}
