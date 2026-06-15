package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Channel
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels WHERE isActive = 1 ORDER BY (CASE WHEN category LIKE 'FIFA%' THEN 0 WHEN country = 'Bangladesh' THEN 1 WHEN country = 'India' THEN 2 ELSE 3 END) ASC, responseTimeMs ASC, name ASC")
    fun getAllChannelsFlow(): Flow<List<Channel>>

    @Query("SELECT * FROM channels")
    suspend fun getAllRawChannels(): List<Channel>

    @Query("SELECT * FROM channels WHERE isActive = 1 AND category LIKE :category || '%' ORDER BY (CASE WHEN category LIKE 'FIFA%' THEN 0 WHEN country = 'Bangladesh' THEN 1 WHEN country = 'India' THEN 2 ELSE 3 END) ASC, responseTimeMs ASC, name ASC")
    fun getChannelsByCategoryFlow(category: String): Flow<List<Channel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<Channel>)

    @Query("UPDATE channels SET isActive = :isActive WHERE url = :url")
    suspend fun updateChannelStatus(url: String, isActive: Boolean)

    @Query("UPDATE channels SET isActive = :isActive, responseTimeMs = :responseTimeMs WHERE url = :url")
    suspend fun updateChannelValidation(url: String, isActive: Boolean, responseTimeMs: Long)

    @Query("DELETE FROM channels")
    suspend fun deleteAllChannels()
}
