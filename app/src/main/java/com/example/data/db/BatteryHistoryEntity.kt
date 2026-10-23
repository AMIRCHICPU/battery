package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battery_history")
data class BatteryHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val level: Int,
    val isCharging: Boolean,
    val status: String,
    val temperatureCelsius: Float,
    val voltageMilliVolts: Int
)
