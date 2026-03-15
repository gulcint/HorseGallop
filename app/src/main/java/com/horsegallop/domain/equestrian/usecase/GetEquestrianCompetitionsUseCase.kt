package com.horsegallop.domain.equestrian.usecase

import com.horsegallop.domain.equestrian.model.EquestrianCompetition
import com.horsegallop.domain.equestrian.repository.EquestrianAgendaRepository
import javax.inject.Inject

class GetEquestrianCompetitionsUseCase @Inject constructor(
    private val repository: EquestrianAgendaRepository
) {
    suspend operator fun invoke(): Result<List<EquestrianCompetition>> = repository.getCompetitions()
}
