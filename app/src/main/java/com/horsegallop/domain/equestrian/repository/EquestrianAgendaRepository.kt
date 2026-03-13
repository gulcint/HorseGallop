package com.horsegallop.domain.equestrian.repository

import com.horsegallop.domain.equestrian.model.EquestrianAnnouncement
import com.horsegallop.domain.equestrian.model.EquestrianCompetition
import com.horsegallop.domain.equestrian.model.FederatedBarnSyncStatus

interface EquestrianAgendaRepository {
    suspend fun getAnnouncements(): Result<List<EquestrianAnnouncement>>
    suspend fun getCompetitions(): Result<List<EquestrianCompetition>>
    suspend fun getFederatedBarnSyncStatus(): Result<FederatedBarnSyncStatus>
}
