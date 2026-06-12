package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Sponsor
import kotlinx.coroutines.flow.Flow

@Dao
interface SponsorDao {
    @Query("SELECT * FROM sponsors ORDER BY updatedAt DESC")
    fun getAllSponsorsFlow(): Flow<List<Sponsor>>

    @Query("SELECT * FROM sponsors WHERE isActive = 1 ORDER BY updatedAt DESC")
    fun getActiveSponsorsFlow(): Flow<List<Sponsor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSponsors(sponsors: List<Sponsor>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSponsor(sponsor: Sponsor)

    @Query("DELETE FROM sponsors WHERE id = :id")
    suspend fun deleteSponsorById(id: String)

    @Query("DELETE FROM sponsors")
    suspend fun deleteAllSponsors()
}
