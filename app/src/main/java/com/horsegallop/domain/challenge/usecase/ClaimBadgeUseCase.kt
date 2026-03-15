package com.horsegallop.domain.challenge.usecase

import com.horsegallop.domain.challenge.model.Badge
import com.horsegallop.domain.challenge.model.BadgeType
import com.horsegallop.domain.challenge.repository.ChallengeRepository
import javax.inject.Inject

class ClaimBadgeUseCase @Inject constructor(
    private val repo: ChallengeRepository
) {
    suspend operator fun invoke(userId: String, badgeType: BadgeType): Result<Badge> =
        repo.claimBadge(userId, badgeType)
}
