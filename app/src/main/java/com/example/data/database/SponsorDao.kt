package com.example.data.database

import androidx.room.*
import com.example.data.model.Sponsor
import kotlinx.coroutines.flow.Flow

@Dao
interface SponsorDao {
    @Query("SELECT * FROM sponsors WHERE isActive = 1 ORDER BY id DESC")
    fun getAllActiveSponsors(): Flow<List<Sponsor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSponsor(sponsor: Sponsor)

    @Delete
    suspend fun deleteSponsor(sponsor: Sponsor)

    @Query("DELETE FROM sponsors")
    suspend fun deleteAllSponsors()
}
