package com.example.healthypettracker.data.local

import androidx.room.TypeConverter
import com.example.healthypettracker.data.local.entity.DiaryCategory
import com.example.healthypettracker.data.local.entity.FoodType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it) }

    @TypeConverter
    fun fromFoodType(value: FoodType): String = value.name

    @TypeConverter
    fun toFoodType(value: String): FoodType = FoodType.valueOf(value)

    @TypeConverter
    fun fromDiaryCategory(value: DiaryCategory): String = value.name

    @TypeConverter
    fun toDiaryCategory(value: String): DiaryCategory = DiaryCategory.valueOf(value)
}
