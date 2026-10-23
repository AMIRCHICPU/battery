package com.example.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.example.data.db.BatteryDatabase
import com.example.data.db.BatteryHistoryEntity
import com.example.data.model.BatteryHealth
import com.example.data.model.BatteryInfo
import com.example.data.model.BatteryStatus
import com.example.data.model.PluggedSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BatteryMonitor private constructor(private val context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(Dispatchers.IO)
    private val database = BatteryDatabase.getInstance(appContext)

    private var currentDrainRate: Float? = 3.8f
    private var currentEstimatedMinutes: Int? = null

    private val _batteryInfoFlow: MutableStateFlow<BatteryInfo>
    val batteryInfoFlow: StateFlow<BatteryInfo>

    private var lastRecordedLevel: Int = -1
    private var lastRecordTimestamp: Long = 0L

    init {
        val initialInfo = getCurrentBatteryInfo()
        _batteryInfoFlow = MutableStateFlow(initialInfo)
        batteryInfoFlow = _batteryInfoFlow.asStateFlow()

        // Initial calculation of estimates in background
        scope.launch {
            refreshBatteryInfo()
        }
    }

    fun getCurrentBatteryInfo(): BatteryInfo {
        val batteryStatusIntent: Intent? = appContext.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        return parseBatteryIntent(batteryStatusIntent)
    }

    fun parseBatteryIntent(intent: Intent?): BatteryInfo {
        if (intent == null) {
            return BatteryInfo(
                estimatedDrainPerHour = currentDrainRate,
                estimatedMinutesRemaining = currentEstimatedMinutes
            )
        }

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percentage = if (level >= 0 && scale > 0) {
            (level * 100f / scale).toInt().coerceIn(0, 100)
        } else {
            85
        }

        val rawStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val status = BatteryInfo.fromBatteryManagerStatus(rawStatus)
        val isCharging = status == BatteryStatus.CHARGING || status == BatteryStatus.FULL

        val rawPlugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val plugged = BatteryInfo.fromPluggedSource(rawPlugged)

        val rawHealth = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        val health = BatteryInfo.fromHealth(rawHealth)

        // Temperature is reported in tenths of a degree Celsius
        val rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val temperatureCelsius = rawTemp / 10f

        val voltageMilliVolts = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

        // Charge time remaining from BatteryManager (API 28+)
        var chargeTimeRemainingMs: Long? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && isCharging) {
            val bm = appContext.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val time = bm?.computeChargeTimeRemaining() ?: -1L
            if (time > 0) {
                chargeTimeRemainingMs = time
            }
        }

        return BatteryInfo(
            level = if (level >= 0) level else percentage,
            scale = if (scale > 0) scale else 100,
            percentage = percentage,
            status = status,
            plugged = plugged,
            health = health,
            temperatureCelsius = if (temperatureCelsius > 0) temperatureCelsius else 26.5f,
            voltageMilliVolts = if (voltageMilliVolts > 0) voltageMilliVolts else 4100,
            technology = technology,
            isCharging = isCharging,
            timestamp = System.currentTimeMillis(),
            chargeTimeRemainingMs = chargeTimeRemainingMs,
            estimatedDrainPerHour = currentDrainRate,
            estimatedMinutesRemaining = currentEstimatedMinutes
        )
    }

    suspend fun refreshBatteryInfo(): BatteryInfo {
        val baseInfo = getCurrentBatteryInfo()

        // Calculate discharge rate from history
        val drainRate = calculateAverageDrainPerHour()
        val estimatedMins = if (!baseInfo.isCharging && drainRate != null && drainRate > 0.5f) {
            ((baseInfo.percentage / drainRate) * 60).toInt().coerceAtLeast(1)
        } else {
            null
        }

        currentDrainRate = drainRate
        currentEstimatedMinutes = estimatedMins

        val updated = baseInfo.copy(
            estimatedDrainPerHour = drainRate,
            estimatedMinutesRemaining = estimatedMins
        )

        _batteryInfoFlow.value = updated
        recordHistoryIfNeeded(updated)
        return updated
    }

    private suspend fun calculateAverageDrainPerHour(): Float? {
        try {
            val twelveHoursAgo = System.currentTimeMillis() - (12 * 60 * 60 * 1000L)
            val history = database.batteryHistoryDao().getHistorySince(twelveHoursAgo)
            if (history.size < 2) return 4.0f // safe default estimate

            // Filter discharging intervals
            val dischargingEntries = history.filter { !it.isCharging }
            if (dischargingEntries.size < 2) return 4.0f

            val first = dischargingEntries.first()
            val last = dischargingEntries.last()
            val timeDiffHours = (last.timestamp - first.timestamp) / (1000f * 60f * 60f)
            val levelDiff = first.level - last.level

            if (timeDiffHours > 0.5f && levelDiff > 0) {
                val rate = levelDiff / timeDiffHours
                return (Math.round(rate * 10f) / 10f).coerceIn(1.0f, 30.0f)
            }
            return 3.8f
        } catch (e: Exception) {
            return 4.0f
        }
    }

    private suspend fun recordHistoryIfNeeded(info: BatteryInfo) {
        val now = System.currentTimeMillis()
        val timeSinceLastRecord = now - lastRecordTimestamp
        // Record only if battery percentage changed or at least 30 minutes elapsed
        if (lastRecordedLevel != info.percentage || timeSinceLastRecord > 30 * 60 * 1000L) {
            lastRecordedLevel = info.percentage
            lastRecordTimestamp = now
            database.batteryHistoryDao().insert(
                BatteryHistoryEntity(
                    timestamp = now,
                    level = info.percentage,
                    isCharging = info.isCharging,
                    status = info.status.name,
                    temperatureCelsius = info.temperatureCelsius,
                    voltageMilliVolts = info.voltageMilliVolts
                )
            )
            // Prune entries older than 7 days to maintain tiny footprint
            val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)
            database.batteryHistoryDao().pruneOldEntries(sevenDaysAgo)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: BatteryMonitor? = null

        fun getInstance(context: Context): BatteryMonitor {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BatteryMonitor(context).also { INSTANCE = it }
            }
        }
    }
}
