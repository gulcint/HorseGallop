package com.horsegallop.data.equestrian.repository

import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.domain.equestrian.model.EquestrianAnnouncement
import com.horsegallop.domain.equestrian.model.EquestrianCompetition
import com.horsegallop.domain.equestrian.model.FederationManualSyncResult
import com.horsegallop.domain.equestrian.model.FederationSourceHealthItem
import com.horsegallop.domain.equestrian.model.FederatedBarnSyncStatus
import com.horsegallop.domain.equestrian.repository.EquestrianAgendaRepository
import javax.inject.Inject

class EquestrianAgendaRepositoryImpl @Inject constructor(
    private val supabaseDataSource: SupabaseDataSource
) : EquestrianAgendaRepository {

    override suspend fun getAnnouncements(): Result<List<EquestrianAnnouncement>> = runCatching {
        supabaseDataSource.getEquestrianAnnouncements().map { dto ->
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
        supabaseDataSource.getEquestrianCompetitions().map { dto ->
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
        val statusMap = supabaseDataSource.getFederatedBarnsSyncStatus()
        FederatedBarnSyncStatus(
            status = statusMap["status"] as? String ?: "unknown",
            syncedAt = statusMap["syncedAt"] as? String ?: "",
            itemCount = (statusMap["itemCount"] as? Int) ?: 0,
            errorMessage = statusMap["errorMessage"] as? String
        )
    }

    override suspend fun getFederationSourceHealth(): Result<List<FederationSourceHealthItem>> = runCatching {
        // No longer maintained via Cloud Functions; return empty list (data is read from Supabase tables)
        emptyList()
    }

    override suspend fun triggerManualSync(force: Boolean): Result<FederationManualSyncResult> = runCatching {
        // Manual sync is a server-side operation; stub returns last known sync status
        val statusMap = supabaseDataSource.getFederatedBarnsSyncStatus()
        FederationManualSyncResult(
            syncedAt = statusMap["syncedAt"] as? String ?: "",
            barnsCount = 0,
            announcementsCount = 0,
            competitionsCount = 0,
            throttled = false
        )
    }
}
