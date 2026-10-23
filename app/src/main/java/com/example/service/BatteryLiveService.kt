package com.example.service

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.battery.BatteryMonitor
import com.example.data.preferences.WidgetPreferences
import com.example.widget.BatteryWidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BatteryLiveService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var isReceiverRegistered = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                scope.launch {
                    val batteryInfo = BatteryMonitor.getInstance(context).refreshBatteryInfo()
                    BatteryNotificationManager.checkAndNotify(context, batteryInfo)
                    BatteryWidgetUpdater.updateAllWidgets(context)
                    updateNotification(batteryInfo.percentage, batteryInfo.isCharging)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        BatteryNotificationManager.createNotificationChannels(this)
        startForeground(NOTIFICATION_ID, createNotification(85, false))

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)
        isReceiverRegistered = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val monitor = BatteryMonitor.getInstance(this)
        val info = monitor.getCurrentBatteryInfo()
        updateNotification(info.percentage, info.isCharging)
        return START_STICKY
    }

    override fun onDestroy() {
        if (isReceiverRegistered) {
            unregisterReceiver(batteryReceiver)
            isReceiverRegistered = false
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(percentage: Int, isCharging: Boolean) =
        NotificationCompat.Builder(this, BatteryNotificationManager.CHANNEL_SERVICE_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Volt Battery Live ($percentage%)")
            .setContentText(if (isCharging) "Charging • Live updates active" else "Live battery monitoring active")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

    private fun updateNotification(percentage: Int, isCharging: Boolean) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
        manager?.notify(NOTIFICATION_ID, createNotification(percentage, isCharging))
    }

    companion object {
        private const val NOTIFICATION_ID = 2001

        fun startService(context: Context) {
            val intent = Intent(context, BatteryLiveService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, BatteryLiveService::class.java)
            context.stopService(intent)
        }
    }
}
