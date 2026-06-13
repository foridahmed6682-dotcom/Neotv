package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Channel
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels WHERE isActive = 1 ORDER BY country = 'Bangladesh' DESC, country = 'India' DESC, name ASC")
    fun getAllChannelsFlow(): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE isActive = 1 AND category = :category ORDER BY country = 'Bangladesh' DESC, country = 'India' DESC, name ASC")
    fun getChannelsByCategoryFlow(category: String): Flow<List<Channel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<Channel>)

    @Query("UPDATE channels SET isActive = :isActive WHERE url = :url")
    suspend fun updateChannelStatus(url: String, isActive: Boolean)

    @Query("DELETE FROM channels")
    suspend fun deleteAllChannels()
}
