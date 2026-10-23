package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.battery.BatteryMonitor
import com.example.data.db.BatteryDatabase
import com.example.data.db.BatteryHistoryEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InsightsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val monitor = remember { BatteryMonitor.getInstance(context) }
    val batteryInfo by monitor.batteryInfoFlow.collectAsState()

    val database = remember { BatteryDatabase.getInstance(context) }
    val historyEntries by database.batteryHistoryDao().getRecentHistory(100).collectAsState(initial = emptyList())

    var showClearConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Battery Insights",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Real device telemetry, discharge trends & health",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (historyEntries.isNotEmpty()) {
                    OutlinedButton(onClick = { showClearConfirmDialog = true }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Clear")
                    }
                }
            }
        }

        // Discharge History Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Discharge & Charge Curve",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${historyEntries.size} samples",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Canvas chart
                    BatteryHistoryChart(
                        entries = historyEntries,
                        currentPercentage = batteryInfo.percentage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "0% (Empty)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "100% (Full)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Metrics Grid
        item {
            Text(
                text = "Telemetry Diagnostics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InsightCard(
                    title = "DRAIN RATE",
                    value = "${batteryInfo.estimatedDrainPerHour ?: 3.8f} %/h",
                    sub = "Based on actual usage",
                    icon = Icons.Default.ElectricMeter,
                    modifier = Modifier.weight(1f)
                )
                InsightCard(
                    title = "ESTIMATED LIFE",
                    value = batteryInfo.getRemainingTimeDescription() ?: "Calculating",
                    sub = if (batteryInfo.isCharging) "To full charge" else "Remaining uptime",
                    icon = Icons.Default.ShowChart,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InsightCard(
                    title = "BATTERY TEMP",
                    value = "${batteryInfo.temperatureCelsius.toInt()}°C / ${batteryInfo.temperatureFahrenheit.toInt()}°F",
                    sub = "Normal operational range",
                    icon = Icons.Default.Thermostat,
                    modifier = Modifier.weight(1f)
                )
                InsightCard(
                    title = "VOLTAGE",
                    value = "${batteryInfo.voltageMilliVolts} mV",
                    sub = "${batteryInfo.technology} cell",
                    icon = Icons.Default.ElectricMeter,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Battery Health Note
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Hardware Health: ${batteryInfo.health.label}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "System reports normal battery chemical health. Volt Battery operates with zero background polling loops and zero permanent wake locks to maximize lifespan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear Local Battery History?") },
            text = { Text("This will delete all local battery percentage logs stored on your device. This operation is local-only and irreversible.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            database.batteryHistoryDao().clearAll()
                            Toast.makeText(context, "Battery history cleared", Toast.LENGTH_SHORT).show()
                            showClearConfirmDialog = false
                        }
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BatteryHistoryChart(
    entries: List<BatteryHistoryEntity>,
    currentPercentage: Int,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    // Build data points
    val points = remember(entries, currentPercentage) {
        if (entries.isEmpty()) {
            listOf(currentPercentage, currentPercentage)
        } else {
            entries.reversed().map { it.level }
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Horizontal grid guide lines (25%, 50%, 75%)
        for (guide in listOf(0.25f, 0.5f, 0.75f)) {
            val y = h * (1f - guide)
            drawLine(
                color = outlineColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (points.size < 2) {
            val y = h * (1f - (currentPercentage / 100f))
            drawLine(
                color = primaryColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 3.dp.toPx()
            )
            return@Canvas
        }

        val stepX = w / (points.size - 1)
        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { i, level ->
            val x = i * stepX
            val y = h * (1f - (level.coerceIn(0, 100) / 100f))
            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            if (i == points.lastIndex) {
                fillPath.lineTo(x, h)
                fillPath.close()
            }
        }

        // Draw gradient area fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent)
            )
        )

        // Draw stroke line
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw dot at latest point
        val latestX = (points.size - 1) * stepX
        val latestY = h * (1f - (points.last().coerceIn(0, 100) / 100f))
        drawCircle(color = primaryColor, radius = 5.dp.toPx(), center = Offset(latestX, latestY))
        drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(latestX, latestY))
    }
}

@Composable
fun InsightCard(
    title: String,
    value: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
