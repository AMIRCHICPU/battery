package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.battery.BatteryMonitor
import com.example.data.preferences.WidgetPreferences
import com.example.widget.BatteryWidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val batteryInfo = BatteryMonitor.getInstance(context).refreshBatteryInfo()
                    BatteryNotificationManager.checkAndNotify(context, batteryInfo)
                    BatteryWidgetUpdater.updateAllWidgets(context)

                    // Restart Live Updates service if user had it enabled
                    val prefs = WidgetPreferences(context)
                    if (prefs.isLiveModeEnabled()) {
                        BatteryLiveService.startService(context)
                    }
                }
            }
        }
    }
}
