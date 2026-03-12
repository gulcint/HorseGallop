package com.horsegallop.domain.notification.usecase

import com.horsegallop.domain.notification.repository.NotificationRepository
import javax.inject.Inject

class MarkNotificationReadUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(id: String) = repository.markAsRead(id)
}
