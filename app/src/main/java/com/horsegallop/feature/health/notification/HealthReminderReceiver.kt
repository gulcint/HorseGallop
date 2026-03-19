package com.horsegallop.feature.health.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class HealthReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = context.getString(
                context.resources.getIdentifier(
                    "health_reminder_channel_name",
                    "string",
                    context.packageName
                )
            )
            val channel = NotificationChannel(
                CHANNEL_ID,
                channelName.ifBlank { "Sağlık Hatırlatmaları" },
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val contentTitle = context.getString(
            context.resources.getIdentifier(
                "health_reminder_upcoming_title",
                "string",
                context.packageName
            )
        ).ifBlank { "Yaklaşan At Sağlık Randevusu" }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(contentTitle)
            .setContentText(title)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(title.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_ID = "health_reminders"
        const val EXTRA_TITLE = "title"
    }
}
