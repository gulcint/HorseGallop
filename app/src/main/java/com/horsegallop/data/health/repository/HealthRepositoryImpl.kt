package com.horsegallop.data.health.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.horsegallop.core.debug.AppLog
import com.horsegallop.domain.health.model.HealthEvent
import com.horsegallop.domain.health.model.HealthEventType
import com.horsegallop.domain.health.repository.HealthRepository
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
class HealthRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : HealthRepository {

    private fun collection() = firestore.collection("healthEvents")

    override fun getHealthEvents(horseId: String?): Flow<List<HealthEvent>> {
        val userId = auth.currentUser?.uid ?: return flowOf(emptyList())

        return callbackFlow<List<HealthEvent>> {
            val query = if (horseId != null) {
                collection()
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("horseId", horseId)
                    .orderBy("scheduledDate", Query.Direction.ASCENDING)
            } else {
                collection()
                    .whereEqualTo("userId", userId)
                    .orderBy("scheduledDate", Query.Direction.ASCENDING)
            }

            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    AppLog.e("HealthRepo", "Firestore error: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val events = snapshot?.documents?.mapNotNull { doc ->
                    runCatching {
                        val typeStr = doc.getString("type") ?: "VET"
                        val type = try {
                            HealthEventType.valueOf(typeStr)
                        } catch (_: Exception) {
                            HealthEventType.VET
                        }
                        HealthEvent(
                            id = doc.id,
                            userId = doc.getString("userId") ?: userId,
                            horseId = doc.getString("horseId").orEmpty(),
                            horseName = doc.getString("horseName").orEmpty(),
                            type = type,
                            scheduledDate = doc.getLong("scheduledDate") ?: 0L,
                            completedDate = doc.getLong("completedDate"),
                            notes = doc.getString("notes").orEmpty(),
                            isCompleted = doc.getBoolean("isCompleted") ?: false
                        )
                    }.getOrNull()
                } ?: emptyList()
                trySend(events)
            }

            awaitClose { listener.remove() }
        }.catch { emit(emptyList()) }
    }

    override suspend fun saveHealthEvent(event: HealthEvent): Result<HealthEvent> = runCatching {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("Not authenticated")
        val id = event.id.ifBlank { UUID.randomUUID().toString() }
        val data = mapOf(
            "userId" to userId,
            "horseId" to event.horseId,
            "horseName" to event.horseName,
            "type" to event.type.name,
            "scheduledDate" to event.scheduledDate,
            "completedDate" to event.completedDate,
            "notes" to event.notes,
            "isCompleted" to event.isCompleted
        )
        collection().document(id).set(data).await()
        event.copy(id = id, userId = userId)
    }

    override suspend fun deleteHealthEvent(eventId: String): Result<Unit> = runCatching {
        collection().document(eventId).delete().await()
    }

    override suspend fun markCompleted(eventId: String, completedDate: Long): Result<Unit> = runCatching {
        collection().document(eventId).update(
            mapOf(
                "isCompleted" to true,
                "completedDate" to completedDate
            )
        ).await()
    }
}
