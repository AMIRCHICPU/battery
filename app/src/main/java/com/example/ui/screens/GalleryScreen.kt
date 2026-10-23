package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.battery.BatteryMonitor
import com.example.data.model.DesignStyle
import com.example.data.model.WidgetConfig
import com.example.data.preferences.WidgetPreferences
import com.example.widget.BatteryWidgetUpdater
import com.example.widget.WidgetRenderer

@Composable
fun GalleryScreen(
    onNavigateToStudio: (DesignStyle) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { WidgetPreferences(context) }
    val monitor = remember { BatteryMonitor.getInstance(context) }
    val batteryInfo by monitor.batteryInfoFlow.collectAsState()
    val activeConfig by prefs.activeConfigFlow.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Widget Gallery",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "12 genuinely distinct battery widget designs. Each design features unique geometry, percentage presentation, and visualization.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(DesignStyle.entries) { style ->
            val presetConfig = remember(style) { WidgetConfig.getPresetForStyle(style) }
            val isCurrentActive = activeConfig.style == style

            val previewBitmap = remember(presetConfig, batteryInfo) {
                val heightDp = if (style == DesignStyle.DASHBOARD || style == DesignStyle.VERTICAL_BATTERY) 120 else 90
                WidgetRenderer.renderWidgetBitmap(
                    context = context,
                    config = presetConfig,
                    battery = batteryInfo,
                    widthDp = 280,
                    heightDp = heightDp
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(
                        width = if (isCurrentActive) 2.dp else 1.dp,
                        color = if (isCurrentActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(22.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = style.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = style.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        SuggestionChip(
                            onClick = {},
                            label = { Text(style.category) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                labelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    // Render preview canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F172A))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = previewBitmap.asImageBitmap(),
                            contentDescription = "${style.displayName} preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (style == DesignStyle.DASHBOARD || style == DesignStyle.VERTICAL_BATTERY) 120.dp else 90.dp)
                        )
                    }

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigateToStudio(style) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Studio")
                        }

                        Button(
                            onClick = {
                                prefs.saveActiveConfig(presetConfig)
                                BatteryWidgetUpdater.updateAllWidgets(context)
                                Toast.makeText(context, "Applied ${style.displayName} to home screen widgets", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isCurrentActive) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Active")
                            } else {
                                Text("Apply")
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
        }
    }
}
