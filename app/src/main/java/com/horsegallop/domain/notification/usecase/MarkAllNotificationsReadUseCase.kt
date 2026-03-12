package com.horsegallop.domain.notification.usecase

import com.horsegallop.domain.notification.repository.NotificationRepository
import javax.inject.Inject

class MarkAllNotificationsReadUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke() = repository.markAllAsRead()
}
