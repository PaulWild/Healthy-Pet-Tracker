package com.example.healthypettracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.healthypettracker.data.local.dao.CatDao
import com.example.healthypettracker.data.local.dao.DiaryDao
import com.example.healthypettracker.data.local.dao.FoodDao
import com.example.healthypettracker.data.local.dao.MedicineDao
import com.example.healthypettracker.data.local.dao.WeightDao
import com.example.healthypettracker.data.local.entity.Cat
import com.example.healthypettracker.data.local.entity.DiaryNote
import com.example.healthypettracker.data.local.entity.FoodEntry
import com.example.healthypettracker.data.local.entity.Medicine
import com.example.healthypettracker.data.local.entity.MedicineLog
import com.example.healthypettracker.data.local.entity.MedicineSchedule
import com.example.healthypettracker.data.local.entity.WeightEntry

@Database(
    entities = [
        Cat::class,
        Medicine::class,
        MedicineSchedule::class,
        MedicineLog::class,
        WeightEntry::class,
        FoodEntry::class,
        DiaryNote::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun catDao(): CatDao
    abstract fun medicineDao(): MedicineDao
    abstract fun weightDao(): WeightDao
    abstract fun foodDao(): FoodDao
    abstract fun diaryDao(): DiaryDao
}
