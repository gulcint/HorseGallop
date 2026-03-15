package com.horsegallop.domain.challenge.usecase

import com.horsegallop.domain.challenge.model.Badge
import com.horsegallop.domain.challenge.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEarnedBadgesUseCase @Inject constructor(
    private val repo: ChallengeRepository
) {
    operator fun invoke(userId: String): Flow<List<Badge>> =
        repo.getEarnedBadges(userId)
}
