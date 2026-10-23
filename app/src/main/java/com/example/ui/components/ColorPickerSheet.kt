package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ColorPickerDialog(
    title: String,
    initialColorLong: Long,
    onColorSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var hexText by remember {
        mutableStateOf(String.format("%08X", initialColorLong))
    }
    var currentAlpha by remember {
        mutableFloatStateOf(((initialColorLong shr 24) and 0xFF) / 255f)
    }

    val presetColors = listOf(
        0xFF00E676L, // Vibrant Green
        0xFF00F5D4L, // Electric Cyan
        0xFF00E5FFL, // Cyan Light
        0xFF1E90FFL, // Dodger Blue
        0xFFFFB300L, // Amber
        0xFFFF5252L, // Coral Red
        0xFFFF0055L, // Neon Pink
        0xFF9D4EDDL, // Cyber Purple
        0xFFFFFFFFL, // Pure White
        0xFF94A3B8L, // Slate Silver
        0xEE1A1C22L, // One UI Dark Card
        0xCC0D131FL, // Midnight Dark
        0xFF000000L, // AMOLED Black
        0x00000000L  // Transparent
    )

    fun calculateColor(): Long {
        return try {
            val parsed = hexText.trim().removePrefix("#").toLong(16)
            if (hexText.length <= 6) {
                val a = (currentAlpha * 255).toInt().coerceIn(0, 255)
                ((a.toLong() shl 24) or (parsed and 0xFFFFFFL))
            } else {
                parsed
            }
        } catch (e: Exception) {
            initialColorLong
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Color preview badge
                val activeColorLong = calculateColor()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(activeColorLong))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    )
                    Column {
                        Text(
                            text = "#" + String.format("%08X", activeColorLong),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Opacity: ${(currentAlpha * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Opacity Slider
                Column {
                    Text(text = "Transparency / Opacity", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = currentAlpha,
                        onValueChange = { currentAlpha = it },
                        valueRange = 0f..1f
                    )
                }

                // Palette presets
                Text(text = "Presets", style = MaterialTheme.typography.labelMedium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presetColors) { colorValue ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorValue))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                                .clickable {
                                    hexText = String.format("%08X", colorValue)
                                    currentAlpha = ((colorValue shr 24) and 0xFF) / 255f
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (colorValue == activeColorLong) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (Color(colorValue).luminance() > 0.5f) Color.Black else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Hex input
                OutlinedTextField(
                    value = hexText,
                    onValueChange = { hexText = it.take(8).uppercase() },
                    label = { Text("HEX Color (AARRGGBB)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onColorSelected(calculateColor())
                    onDismiss()
                }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun Color.luminance(): Float {
    return (0.299f * red + 0.587f * green + 0.114f * blue)
}
