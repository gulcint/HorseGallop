package com.horsegallop.data.notification.repository

import com.horsegallop.domain.notification.model.AppNotification
import com.horsegallop.domain.notification.model.NotificationType
import com.horsegallop.domain.notification.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor() : NotificationRepository {

    private val _notifications = MutableStateFlow(mockNotifications())

    override fun getNotifications(): Flow<List<AppNotification>> = _notifications.asStateFlow()

    override suspend fun markAsRead(id: String) {
        _notifications.update { list ->
            list.map { if (it.id == id) it.copy(isRead = true) else it }
        }
    }

    override suspend fun markAllAsRead() {
        _notifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
    }

    private fun mockNotifications(): List<AppNotification> {
        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        val day = 86_400_000L
        return listOf(
            AppNotification(
                id = "notif_1",
                type = NotificationType.RESERVATION,
                title = "Rezervasyonunuz Onaylandı",
                body = "25 Mart Salı günü saat 10:00'daki ders rezervasyonunuz onaylandı.",
                timestamp = now - 2 * hour,
                isRead = false
            ),
            AppNotification(
                id = "notif_2",
                type = NotificationType.LESSON,
                title = "Ders Yarın Başlıyor",
                body = "Yarın saat 14:00'te 'İleri Atlama' dersiniz başlıyor. Hazır olun!",
                timestamp = now - 5 * hour,
                isRead = false
            ),
            AppNotification(
                id = "notif_3",
                type = NotificationType.GENERAL,
                title = "HorseGallop\'a Hoş Geldiniz",
                body = "Uygulamaya hoş geldiniz! Atlarınızı kaydedin, dersler rezerve edin ve ilerlemenizi takip edin.",
                timestamp = now - 1 * day,
                isRead = true
            ),
            AppNotification(
                id = "notif_4",
                type = NotificationType.LESSON,
                title = "Ders Değerlendirmesi",
                body = "Geçen haftaki 'Temel Binicilik' dersinizi değerlendirmeyi unutmayın.",
                timestamp = now - 2 * day,
                isRead = false
            ),
            AppNotification(
                id = "notif_5",
                type = NotificationType.RESERVATION,
                title = "İptal Bildirimi",
                body = "28 Mart\'taki ders eğitmenin talebi üzerine iptal edildi. Yeni bir slot seçebilirsiniz.",
                timestamp = now - 3 * day,
                isRead = true
            )
        )
    }
}
