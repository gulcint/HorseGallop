package com.horsegallop.domain.notification.model

data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val timestamp: Long,
    val isRead: Boolean,
    val targetId: String? = null,
    val targetRoute: String? = null
)

enum class NotificationType { GENERAL, RESERVATION, LESSON, HORSE_HEALTH }
