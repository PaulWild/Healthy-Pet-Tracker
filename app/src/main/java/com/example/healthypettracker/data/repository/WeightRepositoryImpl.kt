package com.example.healthypettracker.data.repository

import com.example.healthypettracker.data.local.dao.WeightDao
import com.example.healthypettracker.data.local.entity.WeightEntry
import com.example.healthypettracker.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class WeightRepositoryImpl(private val weightDao: WeightDao) : WeightRepository {
    override fun getWeightEntriesForCat(catId: Long): Flow<List<WeightEntry>> =
        weightDao.getWeightEntriesForCat(catId)

    override fun getRecentWeightEntries(catId: Long, limit: Int): Flow<List<WeightEntry>> =
        weightDao.getRecentWeightEntries(catId, limit)

    override fun getLatestWeightEntry(catId: Long): Flow<WeightEntry?> =
        weightDao.getLatestWeightEntry(catId)

    override suspend fun getWeightEntryById(entryId: Long): WeightEntry? =
        weightDao.getWeightEntryById(entryId)

    override fun getWeightEntriesSince(catId: Long, since: LocalDateTime): Flow<List<WeightEntry>> =
        weightDao.getWeightEntriesSince(catId, since)

    override fun getWeightEntriesForDateRange(
        catId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<WeightEntry>> =
        weightDao.getWeightEntriesForDateRange(catId, startDate, endDate)

    override suspend fun insertWeightEntry(entry: WeightEntry): Long =
        weightDao.insert(entry)

    override suspend fun updateWeightEntry(entry: WeightEntry) =
        weightDao.update(entry)

    override suspend fun deleteWeightEntry(entry: WeightEntry) =
        weightDao.delete(entry)

    override suspend fun deleteWeightEntryById(entryId: Long) =
        weightDao.deleteById(entryId)
}
