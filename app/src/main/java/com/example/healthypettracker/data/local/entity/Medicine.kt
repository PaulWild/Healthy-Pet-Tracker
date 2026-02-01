package com.example.healthypettracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medicines",
    foreignKeys = [
        ForeignKey(
            entity = Cat::class,
            parentColumns = ["id"],
            childColumns = ["catId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("catId")]
)
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val catId: Long,
    val name: String,
    val dosage: String? = null,
    val instructions: String? = null,
    val isActive: Boolean = true
)
