package com.horsegallop.domain.notification.usecase

import com.horsegallop.domain.notification.model.AppNotification
import com.horsegallop.domain.notification.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<List<AppNotification>> = repository.getNotifications()
}
