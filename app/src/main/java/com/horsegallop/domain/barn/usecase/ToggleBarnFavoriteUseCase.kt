package com.horsegallop.domain.barn.usecase

import com.horsegallop.domain.barn.repository.BarnRepository
import javax.inject.Inject

class ToggleBarnFavoriteUseCase @Inject constructor(
    private val repository: BarnRepository
) {
    suspend operator fun invoke(barnId: String) {
        repository.toggleFavorite(barnId)
    }
}
