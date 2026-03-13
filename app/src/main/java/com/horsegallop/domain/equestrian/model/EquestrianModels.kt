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
