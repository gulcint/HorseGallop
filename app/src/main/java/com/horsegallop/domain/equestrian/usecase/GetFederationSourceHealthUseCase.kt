package com.horsegallop.domain.equestrian.usecase

import com.horsegallop.domain.equestrian.repository.EquestrianAgendaRepository
import javax.inject.Inject

class GetFederationSourceHealthUseCase @Inject constructor(
    private val repository: EquestrianAgendaRepository
) {
    suspend operator fun invoke() = repository.getFederationSourceHealth()
}
