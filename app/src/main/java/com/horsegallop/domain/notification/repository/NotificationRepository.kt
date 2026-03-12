package com.horsegallop.domain.notification.repository

import com.horsegallop.domain.notification.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(): Flow<List<AppNotification>>
    suspend fun markAsRead(id: String)
    suspend fun markAllAsRead()
}
