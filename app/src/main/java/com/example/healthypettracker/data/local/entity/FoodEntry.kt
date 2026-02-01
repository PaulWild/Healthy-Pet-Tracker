package com.example.healthypettracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

enum class FoodType {
    DRY,
    WET,
    TREAT,
    OTHER
}

@Entity(
    tableName = "food_entries",
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
data class FoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val catId: Long,
    val foodType: FoodType,
    val brandName: String? = null,
    val amountGrams: Int? = null,
    val fedAt: LocalDateTime
)
