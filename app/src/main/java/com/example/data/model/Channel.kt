package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class Channel(
    @PrimaryKey val url: String,
    val name: String,
    val logoUrl: String? = null,
    val category: String = "All",
    val country: String = "Global",
    val channelNumber: Int = 0
)
