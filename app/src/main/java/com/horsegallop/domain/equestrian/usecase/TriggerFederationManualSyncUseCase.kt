package com.horsegallop.domain.equestrian.usecase

import com.horsegallop.domain.equestrian.model.FederationManualSyncResult
import com.horsegallop.domain.equestrian.repository.EquestrianAgendaRepository
import javax.inject.Inject

class TriggerFederationManualSyncUseCase @Inject constructor(
    private val repository: EquestrianAgendaRepository
) {
    suspend operator fun invoke(): Result<FederationManualSyncResult> = repository.triggerManualSync()
}
