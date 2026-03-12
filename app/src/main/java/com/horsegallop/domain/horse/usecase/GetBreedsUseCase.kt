package com.horsegallop.domain.horse.usecase

import com.horsegallop.domain.horse.repository.HorseRepository
import javax.inject.Inject

class GetBreedsUseCase @Inject constructor(
    private val horseRepository: HorseRepository
) {
    suspend operator fun invoke(locale: String): Result<List<String>> =
        horseRepository.getBreeds(locale)
}
