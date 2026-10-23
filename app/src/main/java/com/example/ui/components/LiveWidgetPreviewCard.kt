package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.model.BatteryInfo
import com.example.data.model.WidgetConfig
import com.example.widget.WidgetRenderer

enum class WidgetPreviewSize(val label: String, val widthDp: Int, val heightDp: Int) {
    COMPACT_2X1("2x1 Compact", 220, 90),
    SQUARE_2X2("2x2 Square", 160, 160),
    WIDE_4X1("4x1 Wide", 320, 90),
    DASHBOARD_4X2("4x2 Dashboard", 320, 170)
}

@Composable
fun LiveWidgetPreviewCard(
    config: WidgetConfig,
    batteryInfo: BatteryInfo,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSize by remember { mutableStateOf(WidgetPreviewSize.COMPACT_2X1) }

    val bitmap = remember(config, batteryInfo, selectedSize) {
        WidgetRenderer.renderWidgetBitmap(
            context = context,
            config = config,
            battery = batteryInfo,
            widthDp = selectedSize.widthDp,
            heightDp = selectedSize.heightDp
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live Home Screen Preview",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${selectedSize.widthDp} x ${selectedSize.heightDp} dp",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(12.dp))

        // Simulated home screen background texture
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (selectedSize == WidgetPreviewSize.DASHBOARD_4X2 || selectedSize == WidgetPreviewSize.SQUARE_2X2) 190.dp else 120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Widget preview render",
                modifier = Modifier
                    .width(selectedSize.widthDp.dp)
                    .height(selectedSize.heightDp.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Dimension toggle chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
        ) {
            WidgetPreviewSize.entries.forEach { size ->
                FilterChip(
                    selected = selectedSize == size,
                    onClick = { selectedSize = size },
                    label = { Text(size.label, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}
