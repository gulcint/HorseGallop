package com.horsegallop.domain.equestrian.usecase

import com.horsegallop.domain.equestrian.model.EquestrianAnnouncement
import com.horsegallop.domain.equestrian.repository.EquestrianAgendaRepository
import javax.inject.Inject

class GetEquestrianAnnouncementsUseCase @Inject constructor(
    private val repository: EquestrianAgendaRepository
) {
    suspend operator fun invoke(): Result<List<EquestrianAnnouncement>> = repository.getAnnouncements()
}
