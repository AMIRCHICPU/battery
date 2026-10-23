package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.BatteryInfo
import com.example.data.preferences.WidgetPreferences

object BatteryNotificationManager {

    const val CHANNEL_ALERTS_ID = "volt_battery_alerts"
    const val CHANNEL_SERVICE_ID = "volt_battery_live_service"
    private const val NOTIFICATION_ID_LOW = 1001
    private const val NOTIFICATION_ID_FULL = 1002

    private var lastNotifiedLowThreshold: Int? = null
    private var lastNotifiedFullThreshold: Int? = null

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return

            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS_ID,
                "Battery Level Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when battery drops below low threshold or reaches full charge"
                enableVibration(true)
            }

            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE_ID,
                "Live Widget Updates",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps battery widgets updated in real time"
                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(alertsChannel)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    fun checkAndNotify(context: Context, batteryInfo: BatteryInfo) {
        val prefs = WidgetPreferences(context)
        val notificationManager = NotificationManagerCompat.from(context)

        // Low battery alert
        if (prefs.isLowBatteryAlertEnabled()) {
            val threshold = prefs.getLowBatteryAlertThreshold()
            if (batteryInfo.percentage <= threshold && !batteryInfo.isCharging) {
                if (lastNotifiedLowThreshold != threshold) {
                    lastNotifiedLowThreshold = threshold
                    postNotification(
                        context = context,
                        notificationId = NOTIFICATION_ID_LOW,
                        title = "Low Battery Alert (${batteryInfo.percentage}%)",
                        message = "Battery has dropped below $threshold%. Plug in your charger."
                    )
                }
            } else if (batteryInfo.percentage > threshold + 5) {
                // Reset flag when charged back up
                lastNotifiedLowThreshold = null
            }
        }

        // Full charge alert
        if (prefs.isFullChargeAlertEnabled()) {
            val threshold = prefs.getFullChargeAlertThreshold()
            if (batteryInfo.percentage >= threshold && batteryInfo.isCharging) {
                if (lastNotifiedFullThreshold != threshold) {
                    lastNotifiedFullThreshold = threshold
                    postNotification(
                        context = context,
                        notificationId = NOTIFICATION_ID_FULL,
                        title = "Charging Target Reached (${batteryInfo.percentage}%)",
                        message = "Your device has reached $threshold% charge. You can safely disconnect."
                    )
                }
            } else if (batteryInfo.percentage < threshold - 5) {
                lastNotifiedFullThreshold = null
            }
        }
    }

    private fun postNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ALERTS_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Notification permission might not be granted yet
        }
    }
}
