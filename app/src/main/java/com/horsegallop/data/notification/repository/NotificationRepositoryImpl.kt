package com.horsegallop.data.notification.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.horsegallop.core.debug.AppLog
import com.horsegallop.domain.notification.model.AppNotification
import com.horsegallop.domain.notification.model.NotificationType
import com.horsegallop.domain.notification.repository.NotificationRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : NotificationRepository {

    override fun getNotifications(): Flow<List<AppNotification>> {
        val userId = auth.currentUser?.uid ?: return flowOf(emptyList())

        return callbackFlow {
            val listener = firestore
                .collection("users").document(userId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        AppLog.e("NotificationRepo", "Firestore error: ${error.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val items = snapshot?.documents?.mapNotNull { doc ->
                        runCatching {
                            AppNotification(
                                id = doc.id,
                                type = when (doc.getString("type")) {
                                    "reservation" -> NotificationType.RESERVATION
                                    "lesson" -> NotificationType.LESSON
                                    else -> NotificationType.GENERAL
                                },
                                title = doc.getString("title").orEmpty(),
                                body = doc.getString("body").orEmpty(),
                                timestamp = doc.getLong("timestamp") ?: 0L,
                                isRead = doc.getBoolean("isRead") ?: false
                            )
                        }.getOrNull()
                    } ?: emptyList()
                    trySend(items)
                }
            awaitClose { listener.remove() }
        }.buffer(Channel.CONFLATED)
    }

    override suspend fun markAsRead(id: String) {
        val userId = auth.currentUser?.uid ?: return
        runCatching {
            firestore
                .collection("users").document(userId)
                .collection("notifications").document(id)
                .update("isRead", true)
                .await()
        }.onFailure {
            AppLog.e("NotificationRepo", "markAsRead failed: ${it.message}")
        }
    }

    override suspend fun markAllAsRead() {
        val userId = auth.currentUser?.uid ?: return
        runCatching {
            val docs = firestore
                .collection("users").document(userId)
                .collection("notifications")
                .whereEqualTo("isRead", false)
                .get()
                .await()
            val batch = firestore.batch()
            docs.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
        }.onFailure {
            AppLog.e("NotificationRepo", "markAllAsRead failed: ${it.message}")
        }
    }
}
