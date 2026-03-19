package com.horsegallop.data.challenge.repository

import com.horsegallop.core.debug.AppLog
import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.data.remote.supabase.SupabaseUserBadgeDto
import com.horsegallop.domain.challenge.model.Badge
import com.horsegallop.domain.challenge.model.BadgeType
import com.horsegallop.domain.challenge.model.Challenge
import com.horsegallop.domain.challenge.model.ChallengeType
import com.horsegallop.domain.challenge.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeRepositoryImpl @Inject constructor(
    private val supabaseDataSource: SupabaseDataSource
) : ChallengeRepository {

    override fun getActiveChallenges(userId: String): Flow<List<Challenge>> {
        if (userId.isBlank()) return flowOf(emptyList())

        return flow {
            val challenges = supabaseDataSource.getActiveChallenges()
            val progressList = supabaseDataSource.getUserChallengeProgress()

            val progressByChallenge = progressList.associateBy { it.challengeId }

            val result = challenges.mapNotNull { dto ->
                runCatching {
                    val type = try {
                        ChallengeType.valueOf(dto.unit.uppercase())
                    } catch (_: Exception) {
                        ChallengeType.MONTHLY_DISTANCE
                    }
                    val progress = progressByChallenge[dto.id]
                    val currentValue = progress?.currentValue ?: 0.0

                    val startDateMs = runCatching {
                        Instant.parse(dto.startDate).toEpochMilli()
                    }.getOrDefault(0L)
                    val endDateMs = runCatching {
                        Instant.parse(dto.endDate).toEpochMilli()
                    }.getOrDefault(0L)

                    Challenge(
                        id = dto.id,
                        type = type,
                        title = dto.title.ifBlank { dto.titleEn },
                        description = dto.description.ifBlank { dto.descriptionEn },
                        targetValue = dto.targetValue,
                        currentValue = currentValue,
                        unit = dto.unit,
                        startDate = startDateMs,
                        endDate = endDateMs,
                        isCompleted = currentValue >= dto.targetValue,
                        reward = null
                    )
                }.onFailure {
                    AppLog.e("ChallengeRepo", "Challenge mapping error: ${it.message}")
                }.getOrNull()
            }
            emit(result)
        }.catch {
            AppLog.e("ChallengeRepo", "getActiveChallenges error: ${it.message}")
            emit(emptyList())
        }
    }

    override fun getEarnedBadges(userId: String): Flow<List<Badge>> {
        if (userId.isBlank()) return flowOf(emptyList())

        return flow {
            val badges = supabaseDataSource.getEarnedBadges()
            val result = badges.mapNotNull { dto ->
                runCatching {
                    val type = try {
                        BadgeType.valueOf(dto.type)
                    } catch (_: Exception) {
                        return@runCatching null
                    }
                    val earnedDateMs = runCatching {
                        Instant.parse(dto.earnedDate).toEpochMilli()
                    }.getOrDefault(0L)
                    Badge(
                        id = dto.id,
                        type = type,
                        earnedDate = earnedDateMs,
                        title = dto.type,
                        description = ""
                    )
                }.getOrNull()
            }
            emit(result)
        }.catch {
            AppLog.e("ChallengeRepo", "getEarnedBadges error: ${it.message}")
            emit(emptyList())
        }
    }

    override suspend fun claimBadge(userId: String, badgeType: BadgeType): Result<Badge> =
        runCatching {
            val now = System.currentTimeMillis()
            val dto = SupabaseUserBadgeDto(
                userId = userId,
                type = badgeType.name,
                earnedDate = Instant.ofEpochMilli(now).toString()
            )
            val saved = supabaseDataSource.insertBadge(dto)
            val earnedDateMs = runCatching {
                Instant.parse(saved.earnedDate).toEpochMilli()
            }.getOrDefault(now)
            Badge(
                id = saved.id,
                type = badgeType,
                earnedDate = earnedDateMs,
                title = badgeType.name,
                description = ""
            )
        }
}
