package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.DesignStyle
import com.example.data.model.WidgetConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray

class WidgetPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("volt_battery_prefs", Context.MODE_PRIVATE)

    private val _activeConfigFlow = MutableStateFlow(getActiveConfig())
    val activeConfigFlow: StateFlow<WidgetConfig> = _activeConfigFlow.asStateFlow()

    private val _isLiveModeFlow = MutableStateFlow(isLiveModeEnabled())
    val isLiveModeFlow: StateFlow<Boolean> = _isLiveModeFlow.asStateFlow()

    fun getActiveConfig(): WidgetConfig {
        val jsonStr = prefs.getString(KEY_ACTIVE_CONFIG, null)
        return if (jsonStr != null) {
            WidgetConfig.fromJson(jsonStr) ?: WidgetConfig.getPresetForStyle(DesignStyle.ONE_UI)
        } else {
            WidgetConfig.getPresetForStyle(DesignStyle.ONE_UI)
        }
    }

    fun saveActiveConfig(config: WidgetConfig) {
        prefs.edit().putString(KEY_ACTIVE_CONFIG, config.toJson()).apply()
        _activeConfigFlow.value = config
    }

    fun getWidgetConfig(appWidgetId: Int): WidgetConfig {
        val jsonStr = prefs.getString(KEY_WIDGET_CONFIG_PREFIX + appWidgetId, null)
        return if (jsonStr != null) {
            WidgetConfig.fromJson(jsonStr) ?: getActiveConfig()
        } else {
            getActiveConfig()
        }
    }

    fun saveWidgetConfig(appWidgetId: Int, config: WidgetConfig) {
        prefs.edit().putString(KEY_WIDGET_CONFIG_PREFIX + appWidgetId, config.toJson()).apply()
    }

    fun removeWidgetConfig(appWidgetId: Int) {
        prefs.edit().remove(KEY_WIDGET_CONFIG_PREFIX + appWidgetId).apply()
    }

    fun getSavedProfiles(): List<WidgetConfig> {
        val list = mutableListOf<WidgetConfig>()
        val jsonArrayStr = prefs.getString(KEY_SAVED_PROFILES, null)
        if (jsonArrayStr != null) {
            try {
                val array = JSONArray(jsonArrayStr)
                for (i in 0 until array.length()) {
                    val cfg = WidgetConfig.fromJson(array.getString(i))
                    if (cfg != null) list.add(cfg)
                }
            } catch (_: Exception) {
            }
        }
        if (list.isEmpty()) {
            // Seed initial presets
            DesignStyle.entries.forEach { style ->
                list.add(WidgetConfig.getPresetForStyle(style))
            }
            saveSavedProfiles(list)
        }
        return list
    }

    fun saveSavedProfiles(profiles: List<WidgetConfig>) {
        val array = JSONArray()
        profiles.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_SAVED_PROFILES, array.toString()).apply()
    }

    fun saveProfile(profile: WidgetConfig) {
        val profiles = getSavedProfiles().toMutableList()
        val index = profiles.indexOfFirst { it.profileId == profile.profileId }
        if (index >= 0) {
            profiles[index] = profile
        } else {
            profiles.add(profile)
        }
        saveSavedProfiles(profiles)
    }

    fun deleteProfile(profileId: String) {
        val profiles = getSavedProfiles().toMutableList()
        profiles.removeAll { it.profileId == profileId }
        saveSavedProfiles(profiles)
    }

    // App Settings
    fun isLiveModeEnabled(): Boolean = prefs.getBoolean(KEY_LIVE_MODE, false)

    fun setLiveModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LIVE_MODE, enabled).apply()
        _isLiveModeFlow.value = enabled
    }

    fun isLowBatteryAlertEnabled(): Boolean = prefs.getBoolean(KEY_LOW_ALERT_ENABLED, true)
    fun setLowBatteryAlertEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_LOW_ALERT_ENABLED, enabled).apply()

    fun getLowBatteryAlertThreshold(): Int = prefs.getInt(KEY_LOW_ALERT_THRESHOLD, 20)
    fun setLowBatteryAlertThreshold(threshold: Int) =
        prefs.edit().putInt(KEY_LOW_ALERT_THRESHOLD, threshold).apply()

    fun isFullChargeAlertEnabled(): Boolean = prefs.getBoolean(KEY_FULL_ALERT_ENABLED, true)
    fun setFullChargeAlertEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_FULL_ALERT_ENABLED, enabled).apply()

    fun getFullChargeAlertThreshold(): Int = prefs.getInt(KEY_FULL_ALERT_THRESHOLD, 100)
    fun setFullChargeAlertThreshold(threshold: Int) =
        prefs.edit().putInt(KEY_FULL_ALERT_THRESHOLD, threshold).apply()

    fun useFahrenheit(): Boolean = prefs.getBoolean(KEY_USE_FAHRENHEIT, false)
    fun setUseFahrenheit(useF: Boolean) = prefs.edit().putBoolean(KEY_USE_FAHRENHEIT, useF).apply()

    fun getLastUpdateTime(): Long = prefs.getLong(KEY_LAST_UPDATE_TIME, 0L)
    fun recordUpdateTime() {
        val count = prefs.getInt(KEY_UPDATE_COUNT, 0) + 1
        prefs.edit()
            .putLong(KEY_LAST_UPDATE_TIME, System.currentTimeMillis())
            .putInt(KEY_UPDATE_COUNT, count)
            .apply()
    }

    fun getUpdateCount(): Int = prefs.getInt(KEY_UPDATE_COUNT, 0)
    fun resetUpdateCount() = prefs.edit().putInt(KEY_UPDATE_COUNT, 0).apply()

    companion object {
        private const val KEY_ACTIVE_CONFIG = "active_config"
        private const val KEY_WIDGET_CONFIG_PREFIX = "widget_cfg_"
        private const val KEY_SAVED_PROFILES = "saved_profiles_json_array"
        private const val KEY_LIVE_MODE = "key_live_mode"
        private const val KEY_LOW_ALERT_ENABLED = "key_low_alert_enabled"
        private const val KEY_LOW_ALERT_THRESHOLD = "key_low_alert_threshold"
        private const val KEY_FULL_ALERT_ENABLED = "key_full_alert_enabled"
        private const val KEY_FULL_ALERT_THRESHOLD = "key_full_alert_threshold"
        private const val KEY_USE_FAHRENHEIT = "key_use_fahrenheit"
        private const val KEY_LAST_UPDATE_TIME = "key_last_update_time"
        private const val KEY_UPDATE_COUNT = "key_update_count"
    }
}
