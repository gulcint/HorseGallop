package com.horsegallop.domain.barnmanagement.usecase

import com.horsegallop.domain.barnmanagement.model.ManagedLesson
import com.horsegallop.domain.barnmanagement.repository.BarnManagementRepository
import javax.inject.Inject

class GetManagedLessonsUseCase @Inject constructor(
    private val repository: BarnManagementRepository
) {
    suspend operator fun invoke(barnId: String): Result<List<ManagedLesson>> =
        repository.getManagedLessons(barnId)
}
