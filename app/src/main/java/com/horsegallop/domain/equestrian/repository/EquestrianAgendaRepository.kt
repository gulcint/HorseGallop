package com.horsegallop.domain.equestrian.repository

import com.horsegallop.domain.equestrian.model.EquestrianAnnouncement
import com.horsegallop.domain.equestrian.model.EquestrianCompetition
import com.horsegallop.domain.equestrian.model.FederationManualSyncResult

interface EquestrianAgendaRepository {
    suspend fun getAnnouncements(): Result<List<EquestrianAnnouncement>>
    suspend fun getCompetitions(): Result<List<EquestrianCompetition>>
    suspend fun triggerManualSync(force: Boolean = false): Result<FederationManualSyncResult>
}
