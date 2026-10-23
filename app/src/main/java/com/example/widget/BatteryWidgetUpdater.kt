package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.battery.BatteryMonitor
import com.example.data.preferences.WidgetPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object BatteryWidgetUpdater {

    fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val prefs = WidgetPreferences(context)
        val config = prefs.getWidgetConfig(appWidgetId)
        val batteryMonitor = BatteryMonitor.getInstance(context)
        val batteryInfo = batteryMonitor.getCurrentBatteryInfo()

        // Get widget dimensions from options
        val options: Bundle? = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 0
        val minHeight = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ?: 0

        val widthDp = if (minWidth > 0) minWidth else 220
        val heightDp = if (minHeight > 0) minHeight else 90

        val bitmap = WidgetRenderer.renderWidgetBitmap(
            context = context,
            config = config,
            battery = batteryInfo,
            widthDp = widthDp,
            heightDp = heightDp
        )

        val views = RemoteViews(context.packageName, R.layout.widget_battery)
        views.setImageViewBitmap(R.id.widget_image, bitmap)

        // PendingIntent to launch MainActivity when widget is tapped
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_APP_WIDGET_ID", appWidgetId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
        prefs.recordUpdateTime()
    }

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
        val componentName = ComponentName(context, BatteryWidgetProvider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(componentName)

        if (widgetIds.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                BatteryMonitor.getInstance(context).refreshBatteryInfo()
                widgetIds.forEach { widgetId ->
                    updateWidget(context, appWidgetManager, widgetId)
                }
            }
        }
    }
}
