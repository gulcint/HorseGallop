package com.horsegallop.domain.notification.model

data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val timestamp: Long,
    val isRead: Boolean
)

enum class NotificationType { GENERAL, RESERVATION, LESSON }
