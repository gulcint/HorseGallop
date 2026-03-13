package com.horsegallop.domain.tjk.usecase

import com.horsegallop.domain.tjk.model.TjkCity
import com.horsegallop.domain.tjk.repository.TjkRepository
import javax.inject.Inject

class GetTjkCitiesUseCase @Inject constructor(
    private val tjkRepository: TjkRepository
) {
    suspend operator fun invoke(): Result<List<TjkCity>> =
        tjkRepository.getCities()
}
