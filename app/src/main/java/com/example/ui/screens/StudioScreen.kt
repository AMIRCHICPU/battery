package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.battery.BatteryMonitor
import com.example.data.model.DesignStyle
import com.example.data.model.WidgetConfig
import com.example.data.preferences.WidgetPreferences
import com.example.ui.components.ColorPickerDialog
import com.example.ui.components.LiveWidgetPreviewCard
import com.example.widget.BatteryWidgetUpdater
import java.util.UUID

@Composable
fun StudioScreen(
    initialStyle: DesignStyle? = null
) {
    val context = LocalContext.current
    val prefs = remember { WidgetPreferences(context) }
    val monitor = remember { BatteryMonitor.getInstance(context) }
    val batteryInfo by monitor.batteryInfoFlow.collectAsState()

    var config by remember {
        mutableStateOf(
            if (initialStyle != null) {
                WidgetConfig.getPresetForStyle(initialStyle)
            } else {
                prefs.getActiveConfig()
            }
        )
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Layout", "Typography", "Colors", "Elements")

    var colorDialogTarget by remember { mutableStateOf<String?>(null) }
    var showSaveProfileDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sticky Live Preview Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            LiveWidgetPreviewCard(
                config = config,
                batteryInfo = batteryInfo
            )
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, style = MaterialTheme.typography.labelLarge) }
                )
            }
        }

        // Tab Content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            when (selectedTab) {
                0 -> { // Layout & Style
                    item {
                        Text(
                            text = "Base Design Style",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(DesignStyle.entries) { style ->
                                FilterChip(
                                    selected = config.style == style,
                                    onClick = {
                                        config = config.copy(
                                            style = style,
                                            profileName = style.displayName
                                        )
                                    },
                                    label = { Text(style.displayName) }
                                )
                            }
                        }
                    }

                    item {
                        SliderControl(
                            label = "Corner Radius",
                            value = config.cornerRadiusDp,
                            valueRange = 0f..36f,
                            unit = "dp",
                            onValueChange = { config = config.copy(cornerRadiusDp = it) }
                        )
                    }

                    item {
                        SliderControl(
                            label = "Border Width",
                            value = config.borderWidthDp,
                            valueRange = 0f..6f,
                            unit = "dp",
                            onValueChange = { config = config.copy(borderWidthDp = it) }
                        )
                    }

                    item {
                        SliderControl(
                            label = "Line / Ring Stroke Thickness",
                            value = config.strokeWidthDp,
                            valueRange = 1f..20f,
                            unit = "dp",
                            onValueChange = { config = config.copy(strokeWidthDp = it) }
                        )
                    }

                    if (config.style == DesignStyle.SEGMENTED_BATTERY || config.style == DesignStyle.PIXEL_RETRO) {
                        item {
                            SliderControl(
                                label = "Segment Count",
                                value = config.segmentCount.toFloat(),
                                valueRange = 5f..20f,
                                unit = "blocks",
                                onValueChange = { config = config.copy(segmentCount = it.toInt()) }
                            )
                        }
                    }

                    item {
                        SliderControl(
                            label = "Internal Padding",
                            value = config.paddingDp,
                            valueRange = 4f..28f,
                            unit = "dp",
                            onValueChange = { config = config.copy(paddingDp = it) }
                        )
                    }

                    item {
                        Text(
                            text = "Percentage Placement",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("CENTER", "INSIDE", "BESIDE").forEach { placement ->
                                FilterChip(
                                    selected = config.percentagePlacement == placement,
                                    onClick = { config = config.copy(percentagePlacement = placement) },
                                    label = { Text(placement) }
                                )
                            }
                        }
                    }
                }

                1 -> { // Typography
                    item {
                        SliderControl(
                            label = "Battery Percentage Font Size",
                            value = config.percentageTextSizeSp,
                            valueRange = 18f..76f,
                            unit = "sp",
                            onValueChange = { config = config.copy(percentageTextSizeSp = it) }
                        )
                    }

                    item {
                        SliderControl(
                            label = "Secondary Label Font Size",
                            value = config.labelTextSizeSp,
                            valueRange = 8f..18f,
                            unit = "sp",
                            onValueChange = { config = config.copy(labelTextSizeSp = it) }
                        )
                    }

                    item {
                        Text(
                            text = "Font Family",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("Default", "RobotoMono", "Orbitron", "Monospace", "Serif")) { font ->
                                FilterChip(
                                    selected = config.fontFamilyName == font,
                                    onClick = { config = config.copy(fontFamilyName = font) },
                                    label = { Text(font) }
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Bold Font Weight", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = config.isBold,
                                onCheckedChange = { config = config.copy(isBold = it) }
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Persian / Eastern Numerals", style = MaterialTheme.typography.bodyLarge)
                                Text("Displays ۸۵٪ instead of 85%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = config.usePersianNumerals,
                                onCheckedChange = { config = config.copy(usePersianNumerals = it) }
                            )
                        }
                    }

                    item {
                        SliderControl(
                            label = "Letter Spacing",
                            value = config.letterSpacing,
                            valueRange = -0.05f..0.3f,
                            unit = "",
                            onValueChange = { config = config.copy(letterSpacing = it) }
                        )
                    }
                }

                2 -> { // Colors
                    item {
                        Text(
                            text = "Colors & Dynamic Thresholds",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap any color chip to customize with HEX input and transparency slider.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    item {
                        ColorTileRow(
                            title = "High Battery Color (> 50%)",
                            colorLong = config.batteryHighColor,
                            onClick = { colorDialogTarget = "HIGH" }
                        )
                    }

                    item {
                        ColorTileRow(
                            title = "Medium Battery Color",
                            colorLong = config.batteryMediumColor,
                            onClick = { colorDialogTarget = "MEDIUM" }
                        )
                    }

                    item {
                        ColorTileRow(
                            title = "Low Battery Color",
                            colorLong = config.batteryLowColor,
                            onClick = { colorDialogTarget = "LOW" }
                        )
                    }

                    item {
                        ColorTileRow(
                            title = "Charging Status Color",
                            colorLong = config.chargingColor,
                            onClick = { colorDialogTarget = "CHARGING" }
                        )
                    }

                    item {
                        ColorTileRow(
                            title = "Background Surface Color",
                            colorLong = config.backgroundColor,
                            onClick = { colorDialogTarget = "BACKGROUND" }
                        )
                    }

                    item {
                        ColorTileRow(
                            title = "Text Color",
                            colorLong = config.textColor,
                            onClick = { colorDialogTarget = "TEXT" }
                        )
                    }

                    item {
                        ColorTileRow(
                            title = "Border Outline Color",
                            colorLong = config.borderColor,
                            onClick = { colorDialogTarget = "BORDER" }
                        )
                    }

                    item {
                        SliderControl(
                            label = "Low Battery Threshold",
                            value = config.lowBatteryThreshold.toFloat(),
                            valueRange = 5f..35f,
                            unit = "%",
                            onValueChange = { config = config.copy(lowBatteryThreshold = it.toInt()) }
                        )
                    }

                    item {
                        SliderControl(
                            label = "Medium Battery Threshold",
                            value = config.mediumBatteryThreshold.toFloat(),
                            valueRange = 36f..70f,
                            unit = "%",
                            onValueChange = { config = config.copy(mediumBatteryThreshold = it.toInt()) }
                        )
                    }
                }

                3 -> { // Elements & Visibility
                    item {
                        Text(
                            text = "Visible Elements",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        ToggleRow(
                            title = "Show Percent Symbol (%)",
                            subtitle = "E.g. '85%' vs '85'",
                            checked = config.showPercentSymbol,
                            onCheckedChange = { config = config.copy(showPercentSymbol = it) }
                        )
                    }

                    item {
                        ToggleRow(
                            title = "Show Charging Bolt",
                            subtitle = "Displays lightning bolt when plugged into charger",
                            checked = config.showChargingIcon,
                            onCheckedChange = { config = config.copy(showChargingIcon = it) }
                        )
                    }

                    item {
                        ToggleRow(
                            title = "Show Status Label",
                            subtitle = "Displays 'Charging' or 'Discharging'",
                            checked = config.showStatusLabel,
                            onCheckedChange = { config = config.copy(showStatusLabel = it) }
                        )
                    }

                    item {
                        ToggleRow(
                            title = "Show Time Estimate",
                            subtitle = "Displays calculated remaining battery life or time to full",
                            checked = config.showTimeEstimate,
                            onCheckedChange = { config = config.copy(showTimeEstimate = it) }
                        )
                    }

                    item {
                        ToggleRow(
                            title = "Show Temperature & Voltage",
                            subtitle = "Real-time °C and mV readouts",
                            checked = config.showTempAndVoltage,
                            onCheckedChange = { config = config.copy(showTempAndVoltage = it) }
                        )
                    }

                    item {
                        ToggleRow(
                            title = "Neon Glow Accent",
                            subtitle = "Soft radiant aura around typography and borders",
                            checked = config.showOutlineGlow,
                            onCheckedChange = { config = config.copy(showOutlineGlow = it) }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }

        // Bottom Action Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        config = WidgetConfig.getPresetForStyle(config.style)
                        Toast.makeText(context, "Reset to default style settings", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(0.9f)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reset")
                }

                OutlinedButton(
                    onClick = {
                        newProfileName = "${config.style.displayName} Variant"
                        showSaveProfileDialog = true
                    },
                    modifier = Modifier.weight(1.1f)
                ) {
                    Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Save Profile")
                }

                Button(
                    onClick = {
                        prefs.saveActiveConfig(config)
                        BatteryWidgetUpdater.updateAllWidgets(context)
                        Toast.makeText(context, "Applied design to home screen widgets!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1.3f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Apply")
                }
            }
        }
    }

    // Color Picker Dialog
    if (colorDialogTarget != null) {
        val target = colorDialogTarget!!
        val currentColor = when (target) {
            "HIGH" -> config.batteryHighColor
            "MEDIUM" -> config.batteryMediumColor
            "LOW" -> config.batteryLowColor
            "CHARGING" -> config.chargingColor
            "BACKGROUND" -> config.backgroundColor
            "TEXT" -> config.textColor
            "BORDER" -> config.borderColor
            else -> 0xFFFFFFFFL
        }

        ColorPickerDialog(
            title = "Customize $target Color",
            initialColorLong = currentColor,
            onColorSelected = { newColor ->
                config = when (target) {
                    "HIGH" -> config.copy(batteryHighColor = newColor)
                    "MEDIUM" -> config.copy(batteryMediumColor = newColor)
                    "LOW" -> config.copy(batteryLowColor = newColor)
                    "CHARGING" -> config.copy(chargingColor = newColor)
                    "BACKGROUND" -> config.copy(backgroundColor = newColor)
                    "TEXT" -> config.copy(textColor = newColor)
                    "BORDER" -> config.copy(borderColor = newColor)
                    else -> config
                }
            },
            onDismiss = { colorDialogTarget = null }
        )
    }

    // Save Profile Dialog
    if (showSaveProfileDialog) {
        AlertDialog(
            onDismissRequest = { showSaveProfileDialog = false },
            title = { Text("Save Custom Profile") },
            text = {
                Column {
                    Text("Give this customized battery widget design a name to save it to your personal profiles collection:")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        label = { Text("Profile Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProfileName.isNotBlank()) {
                            val saved = config.copy(
                                profileId = "custom_" + UUID.randomUUID().toString().take(8),
                                profileName = newProfileName.trim()
                            )
                            prefs.saveProfile(saved)
                            Toast.makeText(context, "Saved profile '${saved.profileName}'", Toast.LENGTH_SHORT).show()
                            showSaveProfileDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SliderControl(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    unit: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${(Math.round(value * 10f) / 10f)} $unit".trim(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

@Composable
fun ColorTileRow(
    title: String,
    colorLong: Long,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "#" + String.format("%08X", colorLong),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(colorLong))
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
        }
    }
}

@Composable
fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
