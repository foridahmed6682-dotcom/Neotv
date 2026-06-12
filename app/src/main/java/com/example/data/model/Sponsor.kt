package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sponsors")
data class Sponsor(
    @PrimaryKey val id: String = "",
    val imageUrl: String = "",
    val text: String = "",
    val linkUrl: String = "",
    val isActive: Boolean = true,
    val updatedAt: Long = 0L
)
