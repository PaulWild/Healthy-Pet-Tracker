package com.example.healthypettracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    tableName = "medicine_schedules",
    foreignKeys = [
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicineId")]
)
data class MedicineSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicineId: Long,
    val scheduledTime: LocalTime,
    val daysOfWeek: Int = 0b1111111, // Bitmask: Sun=1, Mon=2, Tue=4, Wed=8, Thu=16, Fri=32, Sat=64
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
) {
    companion object {
        const val SUNDAY = 1
        const val MONDAY = 2
        const val TUESDAY = 4
        const val WEDNESDAY = 8
        const val THURSDAY = 16
        const val FRIDAY = 32
        const val SATURDAY = 64
        const val ALL_DAYS = 127
    }

    fun isScheduledOn(dayOfWeek: Int): Boolean {
        val dayBit = when (dayOfWeek) {
            java.time.DayOfWeek.SUNDAY.value -> SUNDAY
            java.time.DayOfWeek.MONDAY.value -> MONDAY
            java.time.DayOfWeek.TUESDAY.value -> TUESDAY
            java.time.DayOfWeek.WEDNESDAY.value -> WEDNESDAY
            java.time.DayOfWeek.THURSDAY.value -> THURSDAY
            java.time.DayOfWeek.FRIDAY.value -> FRIDAY
            java.time.DayOfWeek.SATURDAY.value -> SATURDAY
            else -> 0
        }
        return (daysOfWeek and dayBit) != 0
    }
}
