package com.example.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.BatteryInfo
import com.example.data.model.BatteryStatus
import com.example.data.model.DesignStyle
import com.example.data.model.WidgetConfig
import kotlin.math.cos
import kotlin.math.sin

object WidgetRenderer {

    fun renderWidgetBitmap(
        context: Context,
        config: WidgetConfig,
        battery: BatteryInfo,
        widthDp: Int,
        heightDp: Int
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val widthPx = (widthDp * density).toInt().coerceAtLeast(180)
        val heightPx = (heightDp * density).toInt().coerceAtLeast(80)

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        renderOnCanvas(context, canvas, config, battery, widthPx.toFloat(), heightPx.toFloat(), density)

        return bitmap
    }

    fun renderOnCanvas(
        context: Context,
        canvas: Canvas,
        config: WidgetConfig,
        battery: BatteryInfo,
        width: Float,
        height: Float,
        density: Float
    ) {
        // Draw background
        drawBackground(canvas, config, width, height, density)

        // Select and render specific style
        when (config.style) {
            DesignStyle.ULTRA_MINIMAL -> renderUltraMinimal(context, canvas, config, battery, width, height, density)
            DesignStyle.HORIZONTAL_BATTERY -> renderHorizontalBattery(context, canvas, config, battery, width, height, density)
            DesignStyle.VERTICAL_BATTERY -> renderVerticalBattery(context, canvas, config, battery, width, height, density)
            DesignStyle.CIRCULAR_GAUGE -> renderCircularGauge(context, canvas, config, battery, width, height, density)
            DesignStyle.SEGMENTED_BATTERY -> renderSegmentedBattery(context, canvas, config, battery, width, height, density)
            DesignStyle.ONE_UI -> renderOneUi(context, canvas, config, battery, width, height, density)
            DesignStyle.NEON_HUD -> renderNeonHud(context, canvas, config, battery, width, height, density)
            DesignStyle.GLASS_BATTERY -> renderGlassBattery(context, canvas, config, battery, width, height, density)
            DesignStyle.PIXEL_RETRO -> renderPixelRetro(context, canvas, config, battery, width, height, density)
            DesignStyle.DASHBOARD -> renderDashboard(context, canvas, config, battery, width, height, density)
            DesignStyle.OUTLINE_BATTERY -> renderOutlineBattery(context, canvas, config, battery, width, height, density)
            DesignStyle.ADAPTIVE_SMART -> renderAdaptiveSmart(context, canvas, config, battery, width, height, density)
        }
    }

    private fun drawBackground(
        canvas: Canvas,
        config: WidgetConfig,
        width: Float,
        height: Float,
        density: Float
    ) {
        val bgColor = config.backgroundColor.toInt()
        val cornerRadius = config.cornerRadiusDp * density
        val borderWidth = config.borderWidthDp * density
        val borderColor = config.borderColor.toInt()

        val rect = RectF(borderWidth / 2f, borderWidth / 2f, width - borderWidth / 2f, height - borderWidth / 2f)

        if (Color.alpha(bgColor) > 0) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = bgColor
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)
        }

        if (borderWidth > 0 && Color.alpha(borderColor) > 0) {
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = borderColor
                style = Paint.Style.STROKE
                strokeWidth = borderWidth
            }
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)
        }
    }

    private fun getTypeface(context: Context, config: WidgetConfig): Typeface {
        val baseTypeface = when (config.fontFamilyName) {
            "RobotoMono" -> try {
                ResourcesCompat.getFont(context, R.font.roboto_mono) ?: Typeface.MONOSPACE
            } catch (e: Exception) {
                Typeface.MONOSPACE
            }
            "Orbitron" -> try {
                ResourcesCompat.getFont(context, R.font.orbitron) ?: Typeface.SANS_SERIF
            } catch (e: Exception) {
                Typeface.SANS_SERIF
            }
            "Monospace" -> Typeface.MONOSPACE
            "Serif" -> Typeface.SERIF
            else -> Typeface.SANS_SERIF
        }

        val style = if (config.isExtraBold || config.isBold) Typeface.BOLD else Typeface.NORMAL
        return Typeface.create(baseTypeface, style)
    }

    // 1. Ultra Minimal
    private fun renderUltraMinimal(
        context: Context,
        canvas: Canvas,
        config: WidgetConfig,
        battery: BatteryInfo,
        width: Float,
        height: Float,
        density: Float
    ) {
        val activeColor = config.getDynamicColor(battery.percentage, battery.isCharging).toInt()
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.textColor.toInt()
            typeface = getTypeface(context, config)
            textSize = config.percentageTextSizeSp * density
            textAlign = Paint.Align.CENTER
            letterSpacing = config.letterSpacing
        }

        val percentageText = battery.getFormattedPercentage(config.usePersianNumerals, config.showPercentSymbol)
        val textBounds = Rect()
        textPaint.getTextBounds(percentageText, 0, percentageText.length, textBounds)

        val centerY = height / 2f + (textBounds.height() / 2f) - textBounds.bottom
        val centerX = width / 2f

        // Draw center text
        canvas.drawText(percentageText, centerX, centerY, textPaint)

        // Subtle accent progress ring around text or at bottom
        if (config.strokeWidthDp > 0) {
            val stroke = config.strokeWidthDp * density
            val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = config.emptySectionColor.toInt()
                style = Paint.Style.STROKE
                strokeWidth = stroke
                strokeCap = Paint.Cap.ROUND
            }
            val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = activeColor
                style = Paint.Style.STROKE
                strokeWidth = stroke
                strokeCap = Paint.Cap.ROUND
            }

            val radius = (minOf(width, height) / 2f) - (stroke * 1.5f)
            if (radius > 20f * density) {
                val circleRect = RectF(centerX - radius, height / 2f - radius, centerX + radius, height / 2f + radius)
                canvas.drawArc(circleRect, -90f, 360f, false, trackPaint)
                canvas.drawArc(circleRect, -90f, (battery.percentage / 100f) * 360f, false, progressPaint)
            } else {
                // Horizontal accent bar
                val barY = height - 8f * density
                val barWidth = width * 0.6f
                val startX = (width - barWidth) / 2f
                canvas.drawLine(startX, barY, startX + barWidth, barY, trackPaint)
                canvas.drawLine(startX, barY, startX + (barWidth * (battery.percentage / 100f)), barY, progressPaint)
            }
        }

        // Charging icon if charging
        if (battery.isCharging && config.showChargingIcon) {
            drawLightningBolt(canvas, centerX + (textBounds.width() / 2f) + (14f * density), centerY - (textBounds.height() / 2f), 12f * density, activeColor)
        }
    }

    // 2. Horizontal Battery
    private fun renderHorizontalBattery(
        context: Context,
        canvas: Canvas,
        config: WidgetConfig,
        battery: BatteryInfo,
        width: Float,
        height: Float,
        density: Float
    ) {
        val activeColor = config.getDynamicColor(battery.percentage, battery.isCharging).toInt()
        val pad = config.paddingDp * density

        val batteryHeight = minOf(height * 0.5f, 44f * density)
        val tipWidth = 5f * density
        val tipHeight = batteryHeight * 0.45f
        val batteryWidth = (width - (pad * 2f) - tipWidth).coerceAtLeast(60f * density)

        val left = pad
        val top = (height - batteryHeight) / 2f
        val right = left + batteryWidth
        val bottom = top + batteryHeight

        val outerRect = RectF(left, top, right, bottom)
        val corner = 8f * density

        // Draw battery outer shell
        val shellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.textColor.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 2.5f * density
        }
        canvas.drawRoundRect(outerRect, corner, corner, shellPaint)

        // Draw terminal tip (anode)
        val tipRect = RectF(right + 1.5f * density, top + (batteryHeight - tipHeight) / 2f, right + 1.5f * density + tipWidth, top + (batteryHeight + tipHeight) / 2f)
        val tipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.textColor.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(tipRect, 2f * density, 2f * density, tipPaint)

        // Inner fill
        val inset = 3.5f * density
        val fillTotalWidth = (batteryWidth - (inset * 2f))
        val currentFillWidth = fillTotalWidth * (battery.percentage / 100f)
        if (currentFillWidth > 2f) {
            val fillRect = RectF(left + inset, top + inset, left + inset + currentFillWidth, bottom - inset)
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = activeColor
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(fillRect, 4f * density, 4f * density, fillPaint)
        }

        // Percentage text inside or beside
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (battery.percentage > 50 && config.percentagePlacement == "INSIDE") 0xFFFFFFFF.toInt() else config.textColor.toInt()
            typeface = getTypeface(context, config)
            textSize = config.percentageTextSizeSp * density
            textAlign = Paint.Align.CENTER
            setShadowLayer(3f, 0f, 1f, 0x88000000.toInt())
        }

        val text = battery.getFormattedPercentage(config.usePersianNumerals, config.showPercentSymbol)
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val textCenterY = top + (batteryHeight / 2f) + (textBounds.height() / 2f) - textBounds.bottom

        canvas.drawText(text, left + (batteryWidth / 2f), textCenterY, textPaint)

        // Charging indicator
        if (battery.isCharging && config.showChargingIcon) {
            drawLightningBolt(canvas, left + inset + 12f * density, top + (batteryHeight / 2f), 10f * density, 0xFFFFFFFF.toInt())
        }
    }

    // 3. Vertical Battery
    private fun renderVerticalBattery(
        context: Context,
        canvas: Canvas,
        config: WidgetConfig,
        battery: BatteryInfo,
        width: Float,
        height: Float,
        density: Float
    ) {
        val activeColor = config.getDynamicColor(battery.percentage, battery.isCharging).toInt()
        val batteryWidth = minOf(width * 0.45f, 54f * density)
        val capHeight = 5f * density
        val capWidth = batteryWidth * 0.45f
        val batteryHeight = (height * 0.72f).coerceAtLeast(60f * density)

        val left = (width - batteryWidth) / 2f
        val top = (height - batteryHeight) / 2f + (capHeight / 2f)
        val right = left + batteryWidth
        val bottom = top + batteryHeight

        // Top terminal cap
        val capLeft = (width - capWidth) / 2f
        val capRect = RectF(capLeft, top - capHeight, capLeft + capWidth, top)
        val capPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.textColor.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(capRect, 2f * density, 2f * density, capPaint)

        // Main body outline
        val bodyRect = RectF(left, top, right, bottom)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.textColor.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 2.5f * density
        }
        canvas.drawRoundRect(bodyRect, 8f * density, 8f * density, strokePaint)

        // Inner fill from bottom to top
        val inset = 3.5f * density
        val fillTotalHeight = (batteryHeight - (inset * 2f))
        val currentFillHeight = fillTotalHeight * (battery.percentage / 100f)
        if (currentFillHeight > 2f) {
            val fillRect = RectF(left + inset, bottom - inset - currentFillHeight, right - inset, bottom - inset)
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = activeColor
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(fillRect, 4f * density, 4f * density, fillPaint)
        }

        // Percentage text inside
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.textColor.toInt()
            typeface = getTypeface(context, config)
            textSize = (config.percentageTextSizeSp * 0.75f) * density
            textAlign = Paint.Align.CENTER
            setShadowLayer(4f, 0f, 1f, 0xAA000000.toInt())
        }
        val text = battery.getFormattedPercentage(config.usePersianNumerals, config.showPercentSymbol)
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val textCenterY = top + (batteryHeight / 2f) + (textBounds.height() / 2f) - textBounds.bottom
        canvas.drawText(text, width / 2f, textCenterY, textPaint)

        if (battery.isCharging && config.showChargingIcon) {
            drawLightningBolt(canvas, width / 2f, top + 14f * density, 9f * density, 0xFFFFFFFF.toInt())
        }
    }

    // 4. Circular Gauge
    private fun renderCircularGauge(
        context: Context,
        canvas: Canvas,
        config: WidgetConfig,
        battery: BatteryInfo,
        width: Float,
        height: Float,
        density: Float
    ) {
        val activeColor = config.getDynamicColor(battery.percentage, battery.isCharging).toInt()
        val strokeWidth = config.strokeWidthDp * density
        val radius = (minOf(width, height) / 2f) - strokeWidth - (8f * density)
        val centerX = width / 2f
        val centerY = height / 2f

        val arcRect = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        // Background track arc
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.emptySectionColor.toInt()
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawArc(arcRect, 135f, 270f, false, trackPaint)

        // Progress arc
        val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeColor
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            strokeCap = Paint.Cap.ROUND
        }
        val sweepAngle = 270f * (battery.percentage / 100f)
        canvas.drawArc(arcRect, 135f, sweepAngle, false, progressPaint)

        // Center percentage text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.textColor.toInt()
            typeface = getTypeface(context, config)
            textSize = config.percentageTextSizeSp * density
            textAlign = Paint.Align.CENTER
        }
        val text = battery.getFormattedPercentage(config.usePersianNumerals, config.showPercentSymbol)
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val textCenterY = centerY + (textBounds.height() / 2f) - textBounds.bottom - (if (battery.isCharging) 4f * density else 0f)
        canvas.drawText(text, centerX, textCenterY, textPaint)

        // Bottom status or charging indicator
        if (battery.isCharging && config.showChargingIcon) {
            drawLightningBolt(canvas, centerX, centerY + (radius * 0.5f), 10f * density, activeColor)
        } else if (config.showStatusLabel) {
            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = config.accentTextColor.toInt()
                textSize = config.labelTextSizeSp * density
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(battery.status.label, centerX, centerY + (radius * 0.6f), labelPaint)
        }
    }

    // 5. Segmented Battery
    private fun renderSegmentedBattery(
        context: Context,
        canvas: Canvas,
        config: WidgetConfig,
        battery: BatteryInfo,
        width: Float,
        height: Float,
        density: Float
    ) {
        val activeColor = config.getDynamicColor(battery.percentage, battery.isCharging).toInt()
        val segmentCount = config.segmentCount.coerceIn(5, 20)
        val pad = config.paddingDp * density

        val labelHeight = if (config.showStatusLabel) 18f * density else 0f
        val barHeight = minOf(height - (pad * 2f) - labelHeight, 28f * density)
        val barWidth = width - (pad * 2f)
        val spacing = 3.5f * density
        val segmentWidth = (barWidth - (spacing * (segmentCount - 1))) / segmentCount

        val startY = pad + (height - (pad * 2f) - barHeight - labelHeight) / 2f
        val activeSegments = (battery.percentage / 100f * segmentCount).toInt()

        val corner = 3f * density
        val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeColor
            style = Paint.Style.FILL
        }
        val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.emptySectionColor.toInt()
            style = Paint.Style.FILL
        }

        for (i in 0 until segmentCount) {
            val segLeft = pad + i * (segmentWidth + spacing)
            val segRect = RectF(segLeft, startY, segLeft + segmentWidth, startY + barHeight)
            val paint = if (i < activeSegments) activePaint else inactivePaint
            canvas.drawRoundRect(segRect, corner, corner, paint)
        }

        // Percentage text centered below or above
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.textColor.toInt()
            typeface = getTypeface(context, config)
            textSize = config.percentageTextSizeSp * density
            textAlign = Paint.Align.CENTER
        }
        val text = battery.getFormattedPercentage(config.usePersianNumerals, config.showPercentSymbol)
        val textY = startY + barHeight + (20f * density)
        canvas.drawText(text, width / 2f, textY, textPaint)
    }

    // 6. Samsung One UI Inspired
    private fun renderOneUi(
        context: Context,
        canvas: Canvas,
        config: WidgetConfig,
        battery: BatteryInfo,
        width: Float,
        height: Float,
        density: Float
    ) {
        val activeColor = config.getDynamicColor(battery.percentage, battery.isCharging).toInt()
        val pad = config.paddingDp * density

        // Left icon/accent circle
        val iconSize = 38f * density
        val iconX = pad + (iconSize / 2f)
        val iconY = height / 2f

        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x22FFFFFF.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawCircle(iconX, iconY, iconSize / 2f, circlePaint)

        // Lightning or battery icon inside circle
        if (battery.isCharging) {
            drawLightningBolt(canvas, iconX, iconY, 14f * density, activeColor)
        } else {
            drawMiniBatteryIcon(canvas, iconX, iconY, 13f * density, activeColor, battery.percentage)
        }

        // Right side info: Percentage (large) + Subtitle (status or estimate)
        val textStartX = pad + iconSize + (12f * density)
        val primaryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.textColor.toInt()
            typeface = getTypeface(context, config)
            textSize = config.percentageTextSizeSp * density
            textAlign = Paint.Align.LEFT
        }

        val text = battery.getFormattedPercentage(config.usePersianNumerals, config.showPercentSymbol)
        val textBounds = Rect()
        primaryPaint.getTextBounds(text, 0, text.length, textBounds)

        val topTextY = height / 2f - (2f * density)
        canvas.drawText(text, textStartX, topTextY, primaryPaint)

        // Subtitle: Time estimate or status
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.accentTextColor.toInt()
            textSize = config.labelTextSizeSp * density
            textAlign = Paint.Align.LEFT
        }
        val subText = battery.getRemainingTimeDescription() ?: battery.status.label
        canvas.drawText(subText, textStartX, topTextY + (16f * density), subPaint)

        // One UI pill progress bar across bottom
        val pillHeight = 5f * density
        val pillY = height - pad - pillHeight
        val pillWidth = width - (pad * 2f)

        val trackRect = RectF(pad, pillY, pad + pillWidth, pillY + pillHeight)
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.emptySectionColor.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(trackRect, pillHeight / 2f, pillHeight / 2f, trackPaint)

        val fillWidth = pillWidth * (battery.percentage / 100f)
        if (fillWidth > 2f) {
            val fillRect = RectF(pad, pillY, pad + fillWidth, pillY + pillHeight)
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = activeColor
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(fillRect, pillHeight / 2f, pillHeight / 2f, fillPaint)
        }
    }

    // 7. Neon HUD
    private fun renderNeonHud(
        context: Context,
        canvas: Canvas,
        config: WidgetConfig,
        battery: BatteryInfo,
        width: Float,
        height: Float,
        density: Float
    ) {
        val activeColor = config.getDynamicColor(battery.percentage, battery.isCharging).toInt()

        // Angular brackets in corners
        val bracketPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeColor
            style = Paint.Style.STROKE
            strokeWidth = 2f * density
        }
        val bSize = 14f * density
        val pad = 8f * density

        // Top-Left
        canvas.drawLine(pad, pad + bSize, pad, pad, bracketPaint)
        canvas.drawLine(pad, pad, pad + bSize, pad, bracketPaint)
        // Top-Right
        canvas.drawLine(width - pad - bSize, pad, width - pad, pad, bracketPaint)
        canvas.drawLine(width - pad, pad, width - pad, pad + bSize, bracketPaint)
        // Bottom-Left
        canvas.drawLine(pad, height - pad - bSize, pad, height - pad, bracketPaint)
        canvas.drawLine(pad, height - pad, pad + bSize, height - pad, bracketPaint)
        // Bottom-Right
        canvas.drawLine(width - pad - bSize, height - pad, width - pad, height - pad, bracketPaint)
        canvas.drawLine(width - pad, height - pad, width - pad, height - pad - bSize, bracketPaint)

        // Center typography with glowing neon feel
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeColor
            typeface = getTypeface(context, config)
            textSize = config.percentageTextSizeSp * density
            textAlign = Paint.Align.CENTER
            if (config.showOutlineGlow) {
                setShadowLayer(12f, 0f, 0f, activeColor)
            }
        }
        val text = battery.getFormattedPercentage(config.usePersianNumerals, config.showPercentSymbol)
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val textY = height / 2f + (textBounds.height() / 2f) - textBounds.bottom - (6f * density)
        canvas.drawText(text, width / 2f, textY, textPaint)

        // Telemetry header and footer
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.accentTextColor.toInt()
            typeface = getTypeface(context, config)
            textSize = 9.5f * density
            textAlign = Paint.Align.CENTER
        }
        val topStatus = if (battery.isCharging) "[ PWR CONNECTED ]" else "[ DISCHARGING ]"
        canvas.drawText(topStatus, width / 2f, pad + 12f * density, subPaint)

        val bottomData = "${battery.voltageMilliVolts}mV // ${battery.temperatureCelsius.toInt()}°C"
        canvas.drawText(bottomData, width / 2f, height - pad - 6f * density, subPaint)
    }

    // 8. Glass Battery
    private fun renderGlassBattery(
        context: Context,
        canvas: Canvas,
        config: WidgetConfig,
        battery: BatteryInfo,
        width: Float,
        height: Float,
        density: Float
    ) {
        val activeColor = config.getDynamicColor(battery.percentage, battery.isCharging).toInt()

        // Subtle diagonal specular reflection streak
        val glassPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width, height,
                intArrayOf(0x33FFFFFF.toInt(), 0x08FFFFFF.toInt(), 0x1AFFFFFF.toInt()),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP
            )
            style = Paint.Style.FILL
        }
        val corner = config.cornerRadiusDp * density
        canvas.drawRoundRect(RectF(0f, 0f, width, height), corner, corner, glassPaint)

        // Center percentage with soft shadow
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.textColor.toInt()
            typeface = getTypeface(context, config)
            textSize = config.percentageTextSizeSp * density
            textAlign = Paint.Align.CENTER
            setShadowLayer(6f, 0f, 2f, 0x99000000.toInt())
        }
        val text = battery.getFormattedPercentage(config.usePersianNumerals, config.showPercentSymbol)
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val centerY = height / 2f + (textBounds.height() / 2f) - textBounds.bottom - (4f * density)
        canvas.drawText(text, width / 2f, centerY, textPaint)

        // Glass bar indicator
        val barPad = 24f * density
        val barY = height - (14f * density)
        val barWidth = width - (barPad * 2f)
        val trackRect = RectF(barPad, barY, barPad + barWidth, barY + 4f * density)

        val barTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x33FFFFFF.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(trackRect, 2f * density, 2f * density, barTrackPaint)

        val fillRect = RectF(barPad, barY, barPad + (barWidth * (battery.percentage / 100f)), barY + 4f * density)
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(fillRect, 2f * density, 2f * density, fillPaint)
    }

    // 9. Pixel Retro
    private fun renderPixelRetro(
        context: Context,
        canvas: Canvas,
        config: WidgetConfig,
        battery: BatteryInfo,
        width: Float,
        height: Float,
        density: Float
    ) {
        val activeColor = config.getDynamicColor(battery.percentage, battery.isCharging).toInt()
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeColor
            typeface = getTypeface(context, config)
            textSize = config.percentageTextSizeSp * density
            textAlign = Paint.Align.CENTER
        }

        val text = battery.getFormattedPercentage(config.usePersianNumerals, config.showPercentSymbol)
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        canvas.drawText(text, width / 2f, height * 0.48f, textPaint)

        // Pixelated matrix segments (discrete chunky squares)
        val segCount = 8
        val segSize = 10f * density
        val gap = 4f * density
        val totalSegWidth = (segCount * segSize) + ((segCount - 1) * gap)
        val startX = (width - totalSegWidth) / 2f
        val segY = height * 0.65f

        val activeSegs = (battery.percentage / 100f * segCount).toInt()
        val onPaint = Paint().apply { color = activeColor; style = Paint.Style.FILL }
        val offPaint = Paint().apply { color = 0x3339FF14.toInt(); style = Paint.Style.FILL }

        for (i in 0 until segCount) {
            val sX = startX + i * (segSize + gap)
            val rect = RectF(sX, segY, sX + segSize, segY + segSize)
            canvas.drawRect(rect, if (i < activeSegs) onPaint else offPaint)
        }
    }

    // 10. Dashboard
    private fun renderDashboard(
        context: Context,
        canvas: Canvas,
        config: WidgetConfig,
        battery: BatteryInfo,
        width: Float,
        height: Float,
        density: Float
    ) {
        val activeColor = config.getDynamicColor(battery.percentage, battery.isCharging).toInt()
        val pad = config.paddingDp * density

        // Left quadrant: Radial Arc + Percentage
        val ringDiameter = minOf(width * 0.42f, height - (pad * 2f))
        val ringRadius = (ringDiameter / 2f) - (6f * density)
        val ringCenterX = pad + (ringDiameter / 2f)
        val ringCenterY = height / 2f

        val arcRect = RectF(ringCenterX - ringRadius, ringCenterY - ringRadius, ringCenterX + ringRadius, ringCenterY + ringRadius)
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.emptySectionColor.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 5f * density
        }
        canvas.drawArc(arcRect, 0f, 360f, false, trackPaint)

        val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeColor
            style = Paint.Style.STROKE
            strokeWidth = 5f * density
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawArc(arcRect, -90f, (battery.percentage / 100f) * 360f, false, progressPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.textColor.toInt()
            typeface = getTypeface(context, config)
            textSize = 24f * density
            textAlign = Paint.Align.CENTER
        }
        val text = battery.getFormattedPercentage(config.usePersianNumerals, config.showPercentSymbol)
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        canvas.drawText(text, ringCenterX, ringCenterY + (textBounds.height() / 2f) - textBounds.bottom, textPaint)

        // Right quadrant: Telemetry metrics
        val metricsX = ringCenterX + ringRadius + (16f * density)
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.accentTextColor.toInt()
            textSize = 10f * density
            textAlign = Paint.Align.LEFT
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.textColor.toInt()
            typeface = getTypeface(context, config)
            textSize = 11.5f * density
            textAlign = Paint.Align.LEFT
        }

        val lineSpacing = 16f * density
        var curY = pad + 14f * density

        // Row 1: Status & Source
        canvas.drawText("STATUS", metricsX, curY, labelPaint)
        canvas.drawText(if (battery.isCharging) battery.plugged.label else battery.status.label, metricsX + 50f * density, curY, valuePaint)
        curY += lineSpacing

        // Row 2: Temp & Health
        canvas.drawText("TEMP", metricsX, curY, labelPaint)
        canvas.drawText("${battery.temperatureCelsius.toInt()}°C • ${battery.health.label}", metricsX + 50f * density, curY, valuePaint)
        curY += lineSpacing

        // Row 3: Voltage & Drain
        canvas.drawText("VOLT", metricsX, curY, labelPaint)
        val drainStr = battery.estimatedDrainPerHour?.let { "${it}%/h" } ?: "3.8%/h"
        canvas.drawText("${battery.voltageMilliVolts}mV • $drainStr", metricsX + 50f * density, curY, valuePaint)
        curY += lineSpacing

        // Row 4: Estimate
        val estimate = battery.getRemainingTimeDescription()
        if (estimate != null) {
            canvas.drawText(estimate, metricsX, curY, valuePaint)
        }
    }

    // 11. Outline Battery
    private fun renderOutlineBattery(
        context: Context,
        canvas: Canvas,
        config: WidgetConfig,
        battery: BatteryInfo,
        width: Float,
        height: Float,
        density: Float
    ) {
        val activeColor = config.getDynamicColor(battery.percentage, battery.isCharging).toInt()
        val pad = config.paddingDp * density

        val batHeight = minOf(height * 0.55f, 40f * density)
        val tipWidth = 4f * density
        val tipHeight = batHeight * 0.4f
        val batWidth = width - (pad * 2f) - tipWidth

        val left = pad
        val top = (height - batHeight) / 2f
        val right = left + batWidth
        val bottom = top + batHeight

        // Ultra-thin outline
        val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.textColor.toInt()
            style = Paint.Style.STROKE
            strokeWidth = config.strokeWidthDp * density
        }
        canvas.drawRoundRect(RectF(left, top, right, bottom), 10f * density, 10f * density, outlinePaint)

        // Anode tip outline
        val tipRect = RectF(right + 1f * density, top + (batHeight - tipHeight) / 2f, right + 1f * density + tipWidth, top + (batHeight + tipHeight) / 2f)
        canvas.drawRoundRect(tipRect, 2f * density, 2f * density, outlinePaint)

        // Clean internal line indicating fill
        val inset = 4f * density
        val progressX = (left + inset) + ((batWidth - inset * 2f) * (battery.percentage / 100f))
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeColor
            style = Paint.Style.STROKE
            strokeWidth = 3f * density
            strokeCap = Paint.Cap.ROUND
        }
        val lineY = bottom - inset - 2f * density
        canvas.drawLine(left + inset, lineY, progressX, lineY, linePaint)

        // Text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.textColor.toInt()
            typeface = getTypeface(context, config)
            textSize = config.percentageTextSizeSp * density
            textAlign = Paint.Align.CENTER
        }
        val text = battery.getFormattedPercentage(config.usePersianNumerals, config.showPercentSymbol)
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val centerY = top + (batHeight / 2f) + (textBounds.height() / 2f) - textBounds.bottom - (3f * density)
        canvas.drawText(text, left + (batWidth / 2f), centerY, textPaint)
    }

    // 12. Adaptive Smart Layout
    private fun renderAdaptiveSmart(
        context: Context,
        canvas: Canvas,
        config: WidgetConfig,
        battery: BatteryInfo,
        width: Float,
        height: Float,
        density: Float
    ) {
        val widthDp = width / density
        val heightDp = height / density

        // Adaptively pick presentation based on space
        if (widthDp > 260 && heightDp > 120) {
            // Expanded space -> Dashboard mode
            renderDashboard(context, canvas, config, battery, width, height, density)
        } else if (widthDp > 180) {
            // Horizontal card mode -> One UI mode
            renderOneUi(context, canvas, config, battery, width, height, density)
        } else {
            // Compact cell -> Circular gauge mode
            renderCircularGauge(context, canvas, config, battery, width, height, density)
        }
    }

    // Helper: draw lightning bolt
    private fun drawLightningBolt(canvas: Canvas, cx: Float, cy: Float, size: Float, colorInt: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorInt
            style = Paint.Style.FILL
        }

        val halfW = size * 0.45f
        val halfH = size * 0.75f

        val path = Path().apply {
            moveTo(cx + (halfW * 0.1f), cy - halfH)
            lineTo(cx - halfW, cy + (halfH * 0.1f))
            lineTo(cx - (halfW * 0.1f), cy + (halfH * 0.1f))
            lineTo(cx - (halfW * 0.2f), cy + halfH)
            lineTo(cx + halfW, cy - (halfH * 0.1f))
            lineTo(cx + (halfW * 0.1f), cy - (halfH * 0.1f))
            close()
        }
        canvas.drawPath(path, paint)
    }

    // Helper: draw mini battery icon
    private fun drawMiniBatteryIcon(canvas: Canvas, cx: Float, cy: Float, size: Float, colorInt: Int, level: Int) {
        val w = size * 1.5f
        val h = size
        val left = cx - (w / 2f)
        val top = cy - (h / 2f)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorInt
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRoundRect(RectF(left, top, left + w, top + h), 2f, 2f, strokePaint)

        val fillW = (w - 3f) * (level / 100f)
        if (fillW > 1f) {
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colorInt
                style = Paint.Style.FILL
            }
            canvas.drawRect(RectF(left + 1.5f, top + 1.5f, left + 1.5f + fillW, top + h - 1.5f), fillPaint)
        }
    }
}
