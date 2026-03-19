package com.horsegallop

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.horsegallop.core.debug.AppLog
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PushService : FirebaseMessagingService() {

    @Inject
    lateinit var supabaseClient: SupabaseClient

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        AppLog.i("PushService", "New FCM token: ${token.take(20)}...")
        saveTokenToSupabase(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        AppLog.i("PushService", "FCM received: ${message.notification?.title}")

        val title = message.notification?.title ?: message.data["title"] ?: "HorseGallop"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val type = message.data["type"] ?: ""

        showNotification(title, body, type)
    }

    private fun saveTokenToSupabase(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                supabaseClient.postgrest["fcm_tokens"].upsert(
                    mapOf(
                        "user_id" to userId,
                        "token" to token,
                        "platform" to "android"
                    )
                ) {
                    onConflict = "token"
                }
                AppLog.i("PushService", "FCM token saved to Supabase")
            } catch (e: Exception) {
                AppLog.e("PushService", "FCM token save to Supabase failed: $e")
            }
        }
    }

    private fun showNotification(title: String, body: String, type: String) {
        val channelId = getChannelIdForType(type)
        createNotificationChannel(channelId)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun getChannelIdForType(type: String): String = when (type) {
        "reservation" -> CHANNEL_RESERVATION
        "lesson_reminder" -> CHANNEL_LESSON
        else -> CHANNEL_GENERAL
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val (name, description) = when (channelId) {
                CHANNEL_RESERVATION -> "Rezervasyonlar" to "Rezervasyon onayı ve iptal bildirimleri"
                CHANNEL_LESSON -> "Ders Hatırlatmaları" to "Yaklaşan ders bildirimleri"
                else -> "Genel" to "Genel uygulama bildirimleri"
            }
            val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH).apply {
                this.description = description
                enableVibration(true)
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_GENERAL = "horsegallop_general"
        const val CHANNEL_RESERVATION = "horsegallop_reservation"
        const val CHANNEL_LESSON = "horsegallop_lesson"
    }
}
