package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryHistoryDao {

    @Insert
    suspend fun insert(entry: BatteryHistoryEntity): Long

    @Query("SELECT * FROM battery_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 100): Flow<List<BatteryHistoryEntity>>

    @Query("SELECT * FROM battery_history WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    suspend fun getHistorySince(sinceTimestamp: Long): List<BatteryHistoryEntity>

    @Query("SELECT * FROM battery_history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestEntry(): BatteryHistoryEntity?

    @Query("SELECT COUNT(*) FROM battery_history")
    suspend fun getCount(): Int

    @Query("DELETE FROM battery_history")
    suspend fun clearAll()

    @Query("DELETE FROM battery_history WHERE timestamp < :olderThanTimestamp")
    suspend fun pruneOldEntries(olderThanTimestamp: Long)
}
