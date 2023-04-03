package com.orcuspay.dakpion.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.orcuspay.dakpion.MainActivity
import com.orcuspay.dakpion.R
import com.orcuspay.dakpion.presentation.theme.PrimaryColor

class NotificationHelper(
    private val context: Context,
) {

    companion object {
        const val BUSINESS_NOTIFICATIONS = "BUSINESS_NOTIFICATIONS"
        const val TAG = "dakpionnotifications"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Dakpion"
            val descriptionText = "Get notified about your businesses"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                BUSINESS_NOTIFICATIONS,
                name,
                importance
            ).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(notificationId: Int = 1, title: String, content: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)


        val builder =
            NotificationCompat.Builder(context, BUSINESS_NOTIFICATIONS)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(PrimaryColor.toArgb())
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}