package com.example.healthypettracker.domain.repository

import com.example.healthypettracker.data.local.entity.FoodEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface FoodRepository {
    fun getFoodEntriesForCat(catId: Long): Flow<List<FoodEntry>>
    fun getFoodEntriesForDay(catId: Long, startOfDay: LocalDateTime, endOfDay: LocalDateTime): Flow<List<FoodEntry>>
    suspend fun getFoodEntryById(entryId: Long): FoodEntry?
    fun getRecentFoodEntries(catId: Long, limit: Int): Flow<List<FoodEntry>>
    fun getFoodEntriesForDateRange(catId: Long, startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<FoodEntry>>
    suspend fun insertFoodEntry(entry: FoodEntry): Long
    suspend fun updateFoodEntry(entry: FoodEntry)
    suspend fun deleteFoodEntry(entry: FoodEntry)
    suspend fun deleteFoodEntryById(entryId: Long)
}
