package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BatteryHistoryEntity::class], version = 1, exportSchema = false)
abstract class BatteryDatabase : RoomDatabase() {
    abstract fun batteryHistoryDao(): BatteryHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: BatteryDatabase? = null

        fun getInstance(context: Context): BatteryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BatteryDatabase::class.java,
                    "battery_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
