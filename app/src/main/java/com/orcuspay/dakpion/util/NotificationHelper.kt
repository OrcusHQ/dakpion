package com.orcuspay.dakpion.util

import android.app.Notification
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
        const val SYNC_CHANNEL = "SYNC"
        const val SYNC_NOTIFICATION_ID = 4242
        const val KEEP_ALIVE_CHANNEL = "KEEP_ALIVE"
        const val KEEP_ALIVE_NOTIFICATION_ID = 4243
        const val TAG = "dakpionnotifications"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
            notificationManager.createNotificationChannel(channel)

            // Silent channel for the brief "syncing" notice shown while an
            // expedited sync runs as a foreground service (Android 8–11 only).
            val syncChannel = NotificationChannel(
                SYNC_CHANNEL,
                "Background sync",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shown briefly while payment SMS are being synced"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(syncChannel)

            // Persistent, silent notice that the keep-alive service is running.
            // Low importance so it sits quietly in the shade; no sound, no badge.
            val keepAliveChannel = NotificationChannel(
                KEEP_ALIVE_CHANNEL,
                "Background protection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Dakpion running so payment SMS are forwarded instantly"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(keepAliveChannel)
        }
    }

    fun buildKeepAliveNotification(): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(context, KEEP_ALIVE_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(PrimaryColor.toArgb())
            .setContentTitle("Dakpion is active")
            .setContentText("Watching for payment SMS — forwarded to OrcusPay instantly")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setShowWhen(false)
            .build()
    }

    fun buildSyncNotification(): Notification {
        return NotificationCompat.Builder(context, SYNC_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(PrimaryColor.toArgb())
            .setContentTitle("Syncing payment SMS")
            .setContentText("Forwarding new payment messages to OrcusPay")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .build()
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