package com.example.healthypettracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

enum class DiaryCategory {
    GENERAL,
    HEALTH,
    BEHAVIOR,
    VET_VISIT,
    OTHER
}

@Entity(
    tableName = "diary_notes",
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
data class DiaryNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val catId: Long,
    val title: String,
    val content: String? = null,
    val category: DiaryCategory = DiaryCategory.GENERAL,
    val createdAt: LocalDateTime
)
