package com.horsegallop.domain.challenge.usecase

import com.horsegallop.domain.challenge.model.Challenge
import com.horsegallop.domain.challenge.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveChallengesUseCase @Inject constructor(
    private val repo: ChallengeRepository
) {
    operator fun invoke(userId: String): Flow<List<Challenge>> =
        repo.getActiveChallenges(userId)
}
