package com.horsegallop.domain.challenge.repository

import com.horsegallop.domain.challenge.model.Badge
import com.horsegallop.domain.challenge.model.BadgeType
import com.horsegallop.domain.challenge.model.Challenge
import kotlinx.coroutines.flow.Flow

interface ChallengeRepository {
    fun getActiveChallenges(userId: String): Flow<List<Challenge>>
    fun getEarnedBadges(userId: String): Flow<List<Badge>>
    suspend fun claimBadge(userId: String, badgeType: BadgeType): Result<Badge>
}
