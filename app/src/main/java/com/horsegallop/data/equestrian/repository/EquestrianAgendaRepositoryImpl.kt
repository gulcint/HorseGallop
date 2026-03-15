package com.horsegallop.data.equestrian.repository

import com.horsegallop.data.remote.functions.AppFunctionsDataSource
import com.horsegallop.domain.equestrian.model.EquestrianAnnouncement
import com.horsegallop.domain.equestrian.model.EquestrianCompetition
import com.horsegallop.domain.equestrian.model.FederationManualSyncResult
import com.horsegallop.domain.equestrian.model.FederationSourceHealthItem
import com.horsegallop.domain.equestrian.model.FederatedBarnSyncStatus
import com.horsegallop.domain.equestrian.repository.EquestrianAgendaRepository
import javax.inject.Inject

class EquestrianAgendaRepositoryImpl @Inject constructor(
    private val functionsDataSource: AppFunctionsDataSource
) : EquestrianAgendaRepository {

    override suspend fun getAnnouncements(): Result<List<EquestrianAnnouncement>> = runCatching {
        functionsDataSource.getEquestrianAnnouncements().map { dto ->
            EquestrianAnnouncement(
                id = dto.id,
                title = dto.title,
                summary = dto.summary,
                publishedAtLabel = dto.publishedAtLabel,
                detailUrl = dto.detailUrl,
                imageUrl = dto.imageUrl
            )
        }
    }

    override suspend fun getCompetitions(): Result<List<EquestrianCompetition>> = runCatching {
        functionsDataSource.getEquestrianCompetitions().map { dto ->
            EquestrianCompetition(
                id = dto.id,
                title = dto.title,
                location = dto.location,
                dateLabel = dto.dateLabel,
                detailUrl = dto.detailUrl
            )
        }
    }

    override suspend fun getFederatedBarnSyncStatus(): Result<FederatedBarnSyncStatus> = runCatching {
        val dto = functionsDataSource.getFederatedBarnsSyncStatus()
        FederatedBarnSyncStatus(
            status = dto.status,
            syncedAt = dto.syncedAt,
            itemCount = dto.itemCount,
            errorMessage = dto.errorMessage
        )
    }

    override suspend fun getFederationSourceHealth(): Result<List<FederationSourceHealthItem>> = runCatching {
        functionsDataSource.getFederationSourceHealth().map { dto ->
            FederationSourceHealthItem(
                source = dto.source,
                status = dto.status,
                itemCount = dto.itemCount,
                lastAttemptAt = dto.lastAttemptAt,
                lastSuccessAt = dto.lastSuccessAt,
                dataAgeMinutes = dto.dataAgeMinutes,
                isStale = dto.isStale,
                errorMessage = dto.errorMessage
            )
        }
    }

    override suspend fun triggerManualSync(force: Boolean): Result<FederationManualSyncResult> = runCatching {
        val dto = if (force) {
            functionsDataSource.triggerFederationDebugSync()
        } else {
            functionsDataSource.triggerFederationManualSync()
        }
        FederationManualSyncResult(
            syncedAt = dto.syncedAt,
            barnsCount = dto.barnsCount,
            announcementsCount = dto.announcementsCount,
            competitionsCount = dto.competitionsCount,
            throttled = dto.throttled
        )
    }
}
