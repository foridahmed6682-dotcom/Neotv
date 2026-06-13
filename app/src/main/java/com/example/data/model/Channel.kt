package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class Channel(
    @PrimaryKey val url: String,
    val name: String,
    val logo: String,
    val category: String,
    val country: String = "Global",
    val channelNumber: Int = 0,
    val isActive: Boolean = true,
    val resolution: String = "720p",
    val responseTimeMs: Long = 99999L
)
