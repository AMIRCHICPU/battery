package com.example.data.model

import org.json.JSONObject

data class WidgetConfig(
    val profileId: String = "default",
    val profileName: String = "Ultra Minimal",
    val style: DesignStyle = DesignStyle.ULTRA_MINIMAL,

    // Typography
    val percentageTextSizeSp: Float = 44f,
    val labelTextSizeSp: Float = 12f,
    val fontFamilyName: String = "Default",
    val isBold: Boolean = true,
    val isExtraBold: Boolean = false,
    val letterSpacing: Float = 0f,
    val usePersianNumerals: Boolean = false,
    val showPercentSymbol: Boolean = true,
    val numberFormat: String = "%d%%",

    // Visibility toggles
    val showChargingIcon: Boolean = true,
    val showStatusLabel: Boolean = true,
    val showTimeEstimate: Boolean = false,
    val showTempAndVoltage: Boolean = false,
    val showOutlineGlow: Boolean = false,

    // Colors (ARGB Longs)
    val batteryHighColor: Long = 0xFF00E676,
    val batteryMediumColor: Long = 0xFFFFB300,
    val batteryLowColor: Long = 0xFFFF5252,
    val chargingColor: Long = 0xFF00E5FF,
    val emptySectionColor: Long = 0x2EFFFFFF,
    val backgroundColor: Long = 0xCC0D131F,
    val textColor: Long = 0xFFFFFFFF,
    val accentTextColor: Long = 0xFF94A3B8,
    val borderColor: Long = 0x22FFFFFF,

    // Thresholds
    val lowBatteryThreshold: Int = 20,
    val mediumBatteryThreshold: Int = 50,

    // Geometry & Layout
    val cornerRadiusDp: Float = 18f,
    val borderWidthDp: Float = 1f,
    val strokeWidthDp: Float = 8f,
    val segmentCount: Int = 10,
    val paddingDp: Float = 12f,
    val elementSpacingDp: Float = 8f,
    val contentAlignment: String = "CENTER",
    val percentagePlacement: String = "CENTER"
) {
    fun getDynamicColor(batteryLevel: Int, isCharging: Boolean): Long {
        return if (isCharging) {
            chargingColor
        } else if (batteryLevel <= lowBatteryThreshold) {
            batteryLowColor
        } else if (batteryLevel <= mediumBatteryThreshold) {
            batteryMediumColor
        } else {
            batteryHighColor
        }
    }

    fun toJson(): String {
        val json = JSONObject()
        json.put("profileId", profileId)
        json.put("profileName", profileName)
        json.put("style", style.id)
        json.put("percentageTextSizeSp", percentageTextSizeSp.toDouble())
        json.put("labelTextSizeSp", labelTextSizeSp.toDouble())
        json.put("fontFamilyName", fontFamilyName)
        json.put("isBold", isBold)
        json.put("isExtraBold", isExtraBold)
        json.put("letterSpacing", letterSpacing.toDouble())
        json.put("usePersianNumerals", usePersianNumerals)
        json.put("showPercentSymbol", showPercentSymbol)
        json.put("numberFormat", numberFormat)
        json.put("showChargingIcon", showChargingIcon)
        json.put("showStatusLabel", showStatusLabel)
        json.put("showTimeEstimate", showTimeEstimate)
        json.put("showTempAndVoltage", showTempAndVoltage)
        json.put("showOutlineGlow", showOutlineGlow)
        json.put("batteryHighColor", batteryHighColor)
        json.put("batteryMediumColor", batteryMediumColor)
        json.put("batteryLowColor", batteryLowColor)
        json.put("chargingColor", chargingColor)
        json.put("emptySectionColor", emptySectionColor)
        json.put("backgroundColor", backgroundColor)
        json.put("textColor", textColor)
        json.put("accentTextColor", accentTextColor)
        json.put("borderColor", borderColor)
        json.put("lowBatteryThreshold", lowBatteryThreshold)
        json.put("mediumBatteryThreshold", mediumBatteryThreshold)
        json.put("cornerRadiusDp", cornerRadiusDp.toDouble())
        json.put("borderWidthDp", borderWidthDp.toDouble())
        json.put("strokeWidthDp", strokeWidthDp.toDouble())
        json.put("segmentCount", segmentCount)
        json.put("paddingDp", paddingDp.toDouble())
        json.put("elementSpacingDp", elementSpacingDp.toDouble())
        json.put("contentAlignment", contentAlignment)
        json.put("percentagePlacement", percentagePlacement)
        return json.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): WidgetConfig? {
            return try {
                val json = JSONObject(jsonStr)
                val styleId = json.optString("style", DesignStyle.ULTRA_MINIMAL.id)
                WidgetConfig(
                    profileId = json.optString("profileId", "default"),
                    profileName = json.optString("profileName", "Custom Profile"),
                    style = DesignStyle.fromId(styleId),
                    percentageTextSizeSp = json.optDouble("percentageTextSizeSp", 44.0).toFloat(),
                    labelTextSizeSp = json.optDouble("labelTextSizeSp", 12.0).toFloat(),
                    fontFamilyName = json.optString("fontFamilyName", "Default"),
                    isBold = json.optBoolean("isBold", true),
                    isExtraBold = json.optBoolean("isExtraBold", false),
                    letterSpacing = json.optDouble("letterSpacing", 0.0).toFloat(),
                    usePersianNumerals = json.optBoolean("usePersianNumerals", false),
                    showPercentSymbol = json.optBoolean("showPercentSymbol", true),
                    numberFormat = json.optString("numberFormat", "%d%%"),
                    showChargingIcon = json.optBoolean("showChargingIcon", true),
                    showStatusLabel = json.optBoolean("showStatusLabel", true),
                    showTimeEstimate = json.optBoolean("showTimeEstimate", false),
                    showTempAndVoltage = json.optBoolean("showTempAndVoltage", false),
                    showOutlineGlow = json.optBoolean("showOutlineGlow", false),
                    batteryHighColor = json.optLong("batteryHighColor", 0xFF00E676),
                    batteryMediumColor = json.optLong("batteryMediumColor", 0xFFFFB300),
                    batteryLowColor = json.optLong("batteryLowColor", 0xFFFF5252),
                    chargingColor = json.optLong("chargingColor", 0xFF00E5FF),
                    emptySectionColor = json.optLong("emptySectionColor", 0x2EFFFFFF),
                    backgroundColor = json.optLong("backgroundColor", 0xCC0D131F),
                    textColor = json.optLong("textColor", 0xFFFFFFFF),
                    accentTextColor = json.optLong("accentTextColor", 0xFF94A3B8),
                    borderColor = json.optLong("borderColor", 0x22FFFFFF),
                    lowBatteryThreshold = json.optInt("lowBatteryThreshold", 20),
                    mediumBatteryThreshold = json.optInt("mediumBatteryThreshold", 50),
                    cornerRadiusDp = json.optDouble("cornerRadiusDp", 18.0).toFloat(),
                    borderWidthDp = json.optDouble("borderWidthDp", 1.0).toFloat(),
                    strokeWidthDp = json.optDouble("strokeWidthDp", 8.0).toFloat(),
                    segmentCount = json.optInt("segmentCount", 10),
                    paddingDp = json.optDouble("paddingDp", 12.0).toFloat(),
                    elementSpacingDp = json.optDouble("elementSpacingDp", 8.0).toFloat(),
                    contentAlignment = json.optString("contentAlignment", "CENTER"),
                    percentagePlacement = json.optString("percentagePlacement", "CENTER")
                )
            } catch (e: Exception) {
                null
            }
        }

        fun getPresetForStyle(style: DesignStyle): WidgetConfig {
            return when (style) {
                DesignStyle.ULTRA_MINIMAL -> WidgetConfig(
                    profileId = "preset_ultra_minimal",
                    profileName = "Ultra Minimal",
                    style = DesignStyle.ULTRA_MINIMAL,
                    percentageTextSizeSp = 56f,
                    labelTextSizeSp = 11f,
                    backgroundColor = 0x00000000, // transparent by default
                    borderColor = 0x00000000,
                    borderWidthDp = 0f,
                    showStatusLabel = false,
                    showChargingIcon = true,
                    strokeWidthDp = 4f,
                    fontFamilyName = "Default",
                    isBold = true
                )
                DesignStyle.HORIZONTAL_BATTERY -> WidgetConfig(
                    profileId = "preset_horizontal",
                    profileName = "Horizontal Battery",
                    style = DesignStyle.HORIZONTAL_BATTERY,
                    percentageTextSizeSp = 32f,
                    labelTextSizeSp = 12f,
                    backgroundColor = 0xCC111827,
                    borderColor = 0x334B5563,
                    borderWidthDp = 1.5f,
                    cornerRadiusDp = 20f,
                    strokeWidthDp = 3f,
                    percentagePlacement = "INSIDE"
                )
                DesignStyle.VERTICAL_BATTERY -> WidgetConfig(
                    profileId = "preset_vertical",
                    profileName = "Vertical Battery",
                    style = DesignStyle.VERTICAL_BATTERY,
                    percentageTextSizeSp = 28f,
                    labelTextSizeSp = 11f,
                    backgroundColor = 0xCC111827,
                    borderColor = 0x334B5563,
                    cornerRadiusDp = 22f,
                    percentagePlacement = "INSIDE"
                )
                DesignStyle.CIRCULAR_GAUGE -> WidgetConfig(
                    profileId = "preset_circular_gauge",
                    profileName = "Circular Gauge",
                    style = DesignStyle.CIRCULAR_GAUGE,
                    percentageTextSizeSp = 36f,
                    labelTextSizeSp = 11f,
                    backgroundColor = 0xCC0F172A,
                    borderColor = 0x22334155,
                    borderWidthDp = 1f,
                    cornerRadiusDp = 24f,
                    strokeWidthDp = 9f
                )
                DesignStyle.SEGMENTED_BATTERY -> WidgetConfig(
                    profileId = "preset_segmented",
                    profileName = "Segmented Battery",
                    style = DesignStyle.SEGMENTED_BATTERY,
                    percentageTextSizeSp = 34f,
                    labelTextSizeSp = 12f,
                    backgroundColor = 0xDD0B0F19,
                    borderColor = 0x331E293B,
                    cornerRadiusDp = 16f,
                    segmentCount = 10,
                    strokeWidthDp = 6f
                )
                DesignStyle.ONE_UI -> WidgetConfig(
                    profileId = "preset_one_ui",
                    profileName = "Samsung One UI",
                    style = DesignStyle.ONE_UI,
                    percentageTextSizeSp = 38f,
                    labelTextSizeSp = 13f,
                    backgroundColor = 0xEE1A1C22,
                    borderColor = 0x25FFFFFF,
                    borderWidthDp = 1f,
                    cornerRadiusDp = 26f,
                    batteryHighColor = 0xFF2ED573,
                    batteryMediumColor = 0xFFFFA502,
                    batteryLowColor = 0xFFFF4757,
                    chargingColor = 0xFF1E90FF,
                    showTimeEstimate = true,
                    showStatusLabel = true
                )
                DesignStyle.NEON_HUD -> WidgetConfig(
                    profileId = "preset_neon_hud",
                    profileName = "Neon HUD",
                    style = DesignStyle.NEON_HUD,
                    percentageTextSizeSp = 40f,
                    labelTextSizeSp = 11f,
                    fontFamilyName = "Orbitron",
                    backgroundColor = 0xF5050B14,
                    borderColor = 0x6600E5FF,
                    borderWidthDp = 1.5f,
                    cornerRadiusDp = 10f,
                    batteryHighColor = 0xFF00FF9D,
                    batteryMediumColor = 0xFFFFD600,
                    batteryLowColor = 0xFFFF0055,
                    chargingColor = 0xFF00F5FF,
                    showOutlineGlow = true,
                    showTempAndVoltage = true
                )
                DesignStyle.GLASS_BATTERY -> WidgetConfig(
                    profileId = "preset_glass",
                    profileName = "Glass Battery",
                    style = DesignStyle.GLASS_BATTERY,
                    percentageTextSizeSp = 36f,
                    labelTextSizeSp = 12f,
                    backgroundColor = 0x55FFFFFF,
                    borderColor = 0x55FFFFFF,
                    borderWidthDp = 1.5f,
                    cornerRadiusDp = 24f,
                    textColor = 0xFFFFFFFF,
                    accentTextColor = 0xDDFFFFFF,
                    batteryHighColor = 0xFF4EFA87,
                    chargingColor = 0xFF70D6FF
                )
                DesignStyle.PIXEL_RETRO -> WidgetConfig(
                    profileId = "preset_pixel_retro",
                    profileName = "Pixel Retro",
                    style = DesignStyle.PIXEL_RETRO,
                    percentageTextSizeSp = 34f,
                    labelTextSizeSp = 10f,
                    fontFamilyName = "RobotoMono",
                    backgroundColor = 0xFF111710,
                    borderColor = 0xFF385532,
                    borderWidthDp = 2f,
                    cornerRadiusDp = 4f,
                    batteryHighColor = 0xFF39FF14,
                    batteryMediumColor = 0xFFFFD700,
                    batteryLowColor = 0xFFFF3333,
                    chargingColor = 0xFF00FFFF,
                    segmentCount = 8
                )
                DesignStyle.DASHBOARD -> WidgetConfig(
                    profileId = "preset_dashboard",
                    profileName = "Battery Dashboard",
                    style = DesignStyle.DASHBOARD,
                    percentageTextSizeSp = 38f,
                    labelTextSizeSp = 11f,
                    backgroundColor = 0xEE0D131F,
                    borderColor = 0x33334155,
                    borderWidthDp = 1f,
                    cornerRadiusDp = 22f,
                    showStatusLabel = true,
                    showTimeEstimate = true,
                    showTempAndVoltage = true,
                    strokeWidthDp = 6f
                )
                DesignStyle.OUTLINE_BATTERY -> WidgetConfig(
                    profileId = "preset_outline",
                    profileName = "Outline Battery",
                    style = DesignStyle.OUTLINE_BATTERY,
                    percentageTextSizeSp = 36f,
                    labelTextSizeSp = 12f,
                    backgroundColor = 0x00000000,
                    borderColor = 0x00000000,
                    borderWidthDp = 0f,
                    cornerRadiusDp = 16f,
                    strokeWidthDp = 2.5f,
                    percentagePlacement = "INSIDE"
                )
                DesignStyle.ADAPTIVE_SMART -> WidgetConfig(
                    profileId = "preset_adaptive_smart",
                    profileName = "Adaptive Smart Layout",
                    style = DesignStyle.ADAPTIVE_SMART,
                    percentageTextSizeSp = 38f,
                    labelTextSizeSp = 12f,
                    backgroundColor = 0xDD111827,
                    borderColor = 0x22FFFFFF,
                    borderWidthDp = 1f,
                    cornerRadiusDp = 20f,
                    showStatusLabel = true,
                    showTimeEstimate = true,
                    showTempAndVoltage = true
                )
            }
        }
    }
}
