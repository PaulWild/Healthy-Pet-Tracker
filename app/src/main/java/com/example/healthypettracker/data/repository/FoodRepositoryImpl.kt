package com.example.healthypettracker.data.repository

import com.example.healthypettracker.data.local.dao.FoodDao
import com.example.healthypettracker.data.local.entity.FoodEntry
import com.example.healthypettracker.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class FoodRepositoryImpl(private val foodDao: FoodDao) : FoodRepository {
    override fun getFoodEntriesForCat(catId: Long): Flow<List<FoodEntry>> =
        foodDao.getFoodEntriesForCat(catId)

    override fun getFoodEntriesForDay(
        catId: Long,
        startOfDay: LocalDateTime,
        endOfDay: LocalDateTime
    ): Flow<List<FoodEntry>> =
        foodDao.getFoodEntriesForDay(catId, startOfDay, endOfDay)

    override suspend fun getFoodEntryById(entryId: Long): FoodEntry? =
        foodDao.getFoodEntryById(entryId)

    override fun getRecentFoodEntries(catId: Long, limit: Int): Flow<List<FoodEntry>> =
        foodDao.getRecentFoodEntries(catId, limit)

    override fun getFoodEntriesForDateRange(
        catId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<FoodEntry>> =
        foodDao.getFoodEntriesForDateRange(catId, startDate, endDate)

    override suspend fun insertFoodEntry(entry: FoodEntry): Long =
        foodDao.insert(entry)

    override suspend fun updateFoodEntry(entry: FoodEntry) =
        foodDao.update(entry)

    override suspend fun deleteFoodEntry(entry: FoodEntry) =
        foodDao.delete(entry)

    override suspend fun deleteFoodEntryById(entryId: Long) =
        foodDao.deleteById(entryId)
}
