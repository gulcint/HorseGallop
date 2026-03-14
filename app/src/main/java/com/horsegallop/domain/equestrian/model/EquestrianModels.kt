package com.horsegallop.domain.equestrian.model

data class EquestrianAnnouncement(
    val id: String,
    val title: String,
    val summary: String,
    val publishedAtLabel: String,
    val detailUrl: String,
    val imageUrl: String? = null
)

data class EquestrianCompetition(
    val id: String,
    val title: String,
    val location: String,
    val dateLabel: String,
    val detailUrl: String
)

data class FederatedBarnSyncStatus(
    val status: String,
    val syncedAt: String,
    val itemCount: Int,
    val errorMessage: String? = null
)

data class FederationManualSyncResult(
    val syncedAt: String,
    val barnsCount: Int,
    val announcementsCount: Int,
    val competitionsCount: Int,
    val throttled: Boolean
)

data class FederationSourceHealthItem(
    val source: String,
    val status: String,
    val itemCount: Int,
    val lastAttemptAt: String,
    val lastSuccessAt: String,
    val dataAgeMinutes: Int,
    val errorMessage: String? = null
)
