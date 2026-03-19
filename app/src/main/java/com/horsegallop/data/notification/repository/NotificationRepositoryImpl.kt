package com.horsegallop.data.notification.repository

import com.horsegallop.core.debug.AppLog
import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.data.remote.supabase.SupabaseNotificationDto
import com.horsegallop.domain.notification.model.AppNotification
import com.horsegallop.domain.notification.model.NotificationType
import com.horsegallop.domain.notification.repository.NotificationRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val supabaseDataSource: SupabaseDataSource
) : NotificationRepository {

    override fun getNotifications(): Flow<List<AppNotification>> {
        val userId = supabaseDataSource.currentUserId() ?: return flowOf(emptyList())

        return supabaseDataSource.getNotificationsFlow(userId)
            .map { dtos -> dtos.map { it.toDomain() } }
            .catch { e ->
                AppLog.e("NotificationRepo", "Realtime error: ${e.message}")
                emit(emptyList())
            }
            .buffer(Channel.CONFLATED)
    }

    override suspend fun markAsRead(id: String) {
        supabaseDataSource.markNotificationRead(id)
            .onFailure { AppLog.e("NotificationRepo", "markAsRead failed: ${it.message}") }
    }

    override suspend fun markAllAsRead() {
        val userId = supabaseDataSource.currentUserId() ?: return
        supabaseDataSource.markAllNotificationsRead(userId)
            .onFailure { AppLog.e("NotificationRepo", "markAllAsRead failed: ${it.message}") }
    }

    // ─── helpers ─────────────────────────────────────────────

    private fun SupabaseNotificationDto.toDomain(): AppNotification {
        val timestampMs = runCatching {
            Instant.parse(this.createdAt).toEpochMilli()
        }.getOrDefault(0L)

        val type = when (this.type) {
            "reservation" -> NotificationType.RESERVATION
            "lesson" -> NotificationType.LESSON
            "horse_health" -> NotificationType.HORSE_HEALTH
            else -> NotificationType.GENERAL
        }

        return AppNotification(
            id = this.id,
            type = type,
            title = this.title,
            body = this.body,
            timestamp = timestampMs,
            isRead = this.isRead,
            targetId = null,
            targetRoute = null
        )
    }
}
