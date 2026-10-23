package com.example.data.model

import android.os.BatteryManager

enum class BatteryStatus(val label: String) {
    CHARGING("Charging"),
    DISCHARGING("Discharging"),
    FULL("Full"),
    NOT_CHARGING("Not Charging"),
    UNKNOWN("Unknown")
}

enum class PluggedSource(val label: String) {
    NONE("Unplugged"),
    AC("AC Fast Charger"),
    USB("USB Port"),
    WIRELESS("Wireless Dock")
}

enum class BatteryHealth(val label: String) {
    GOOD("Good"),
    OVERHEAT("Overheated"),
    DEAD("Dead"),
    OVER_VOLTAGE("Over Voltage"),
    UNSPECIFIED_FAILURE("Failure"),
    COLD("Cold"),
    UNKNOWN("Unknown")
}

data class BatteryInfo(
    val level: Int = 85,
    val scale: Int = 100,
    val percentage: Int = 85,
    val status: BatteryStatus = BatteryStatus.DISCHARGING,
    val plugged: PluggedSource = PluggedSource.NONE,
    val health: BatteryHealth = BatteryHealth.GOOD,
    val temperatureCelsius: Float = 27.5f,
    val voltageMilliVolts: Int = 4120,
    val technology: String = "Li-poly",
    val isCharging: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val chargeTimeRemainingMs: Long? = null,
    val estimatedDrainPerHour: Float? = 4.2f,
    val estimatedMinutesRemaining: Int? = 720
) {
    val temperatureFahrenheit: Float
        get() = (temperatureCelsius * 9f / 5f) + 32f

    fun getFormattedPercentage(usePersianNumerals: Boolean = false, includePercentSign: Boolean = true): String {
        val numberStr = if (usePersianNumerals) toPersianDigits(percentage.toString()) else percentage.toString()
        val percentSign = if (includePercentSign) (if (usePersianNumerals) "٪" else "%") else ""
        return "$numberStr$percentSign"
    }

    fun getRemainingTimeDescription(): String? {
        if (isCharging) {
            val chargeMs = chargeTimeRemainingMs
            if (chargeMs != null && chargeMs > 0) {
                val mins = (chargeMs / 1000 / 60).toInt()
                val hours = mins / 60
                val remainingMins = mins % 60
                return if (hours > 0) "${hours}h ${remainingMins}m until full" else "${remainingMins}m until full"
            }
            return if (status == BatteryStatus.FULL) "Fully Charged" else "Charging"
        } else {
            val mins = estimatedMinutesRemaining
            if (mins != null && mins > 0) {
                val hours = mins / 60
                val remainingMins = mins % 60
                return if (hours > 0) "${hours}h ${remainingMins}m remaining" else "${remainingMins}m remaining"
            }
        }
        return null
    }

    companion object {
        fun toPersianDigits(input: String): String {
            val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
            val sb = java.lang.StringBuilder()
            for (ch in input) {
                if (ch in '0'..'9') {
                    sb.append(persianDigits[ch - '0'])
                } else {
                    sb.append(ch)
                }
            }
            return sb.toString()
        }

        fun fromBatteryManagerStatus(status: Int): BatteryStatus = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> BatteryStatus.CHARGING
            BatteryManager.BATTERY_STATUS_DISCHARGING -> BatteryStatus.DISCHARGING
            BatteryManager.BATTERY_STATUS_FULL -> BatteryStatus.FULL
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> BatteryStatus.NOT_CHARGING
            else -> BatteryStatus.UNKNOWN
        }

        fun fromPluggedSource(plugged: Int): PluggedSource = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> PluggedSource.AC
            BatteryManager.BATTERY_PLUGGED_USB -> PluggedSource.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> PluggedSource.WIRELESS
            else -> PluggedSource.NONE
        }

        fun fromHealth(health: Int): BatteryHealth = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
            BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> BatteryHealth.UNSPECIFIED_FAILURE
            BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.COLD
            else -> BatteryHealth.UNKNOWN
        }
    }
}
