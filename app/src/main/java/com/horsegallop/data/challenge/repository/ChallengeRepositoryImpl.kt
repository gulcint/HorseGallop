package com.horsegallop.data.challenge.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.horsegallop.core.debug.AppLog
import com.horsegallop.domain.challenge.model.Badge
import com.horsegallop.domain.challenge.model.BadgeType
import com.horsegallop.domain.challenge.model.Challenge
import com.horsegallop.domain.challenge.model.ChallengeType
import com.horsegallop.domain.challenge.repository.ChallengeRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChallengeRepository {

    private fun challengesCollection() = firestore.collection("challenges")
    private fun badgesCollection() = firestore.collection("badges")

    override fun getActiveChallenges(userId: String): Flow<List<Challenge>> {
        if (userId.isBlank()) return flowOf(emptyList())

        return callbackFlow<List<Challenge>> {
            val query = challengesCollection()
                .whereEqualTo("userId", userId)

            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    AppLog.e("ChallengeRepo", "Firestore challenges error: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val challenges = snapshot?.documents?.mapNotNull { doc ->
                    runCatching {
                        val typeStr = doc.getString("type") ?: "MONTHLY_DISTANCE"
                        val type = try { ChallengeType.valueOf(typeStr) }
                        catch (_: Exception) { ChallengeType.MONTHLY_DISTANCE }
                        val rewardStr = doc.getString("reward")
                        val reward = rewardStr?.let {
                            try { BadgeType.valueOf(it) } catch (_: Exception) { null }
                        }
                        Challenge(
                            id = doc.id,
                            type = type,
                            title = doc.getString("title").orEmpty(),
                            description = doc.getString("description").orEmpty(),
                            targetValue = doc.getDouble("targetValue") ?: 0.0,
                            currentValue = doc.getDouble("currentValue") ?: 0.0,
                            unit = doc.getString("unit").orEmpty(),
                            startDate = doc.getLong("startDate") ?: 0L,
                            endDate = doc.getLong("endDate") ?: 0L,
                            isCompleted = doc.getBoolean("isCompleted") ?: false,
                            reward = reward
                        )
                    }.getOrNull()
                } ?: emptyList()
                trySend(challenges)
            }

            awaitClose { listener.remove() }
        }.catch { emit(emptyList()) }
    }

    override fun getEarnedBadges(userId: String): Flow<List<Badge>> {
        if (userId.isBlank()) return flowOf(emptyList())

        return callbackFlow<List<Badge>> {
            val query = badgesCollection()
                .whereEqualTo("userId", userId)

            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    AppLog.e("ChallengeRepo", "Firestore badges error: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val badges = snapshot?.documents?.mapNotNull { doc ->
                    runCatching {
                        val typeStr = doc.getString("type") ?: return@runCatching null
                        val type = try { BadgeType.valueOf(typeStr) }
                        catch (_: Exception) { return@runCatching null }
                        Badge(
                            id = doc.id,
                            type = type,
                            earnedDate = doc.getLong("earnedDate") ?: 0L,
                            title = doc.getString("title").orEmpty(),
                            description = doc.getString("description").orEmpty()
                        )
                    }.getOrNull()
                } ?: emptyList()
                trySend(badges)
            }

            awaitClose { listener.remove() }
        }.catch { emit(emptyList()) }
    }

    override suspend fun claimBadge(userId: String, badgeType: BadgeType): Result<Badge> = runCatching {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val data = mapOf(
            "userId" to userId,
            "type" to badgeType.name,
            "earnedDate" to now,
            "title" to badgeType.name,
            "description" to ""
        )
        badgesCollection().document(id).set(data).await()
        Badge(
            id = id,
            type = badgeType,
            earnedDate = now,
            title = badgeType.name,
            description = ""
        )
    }
}
