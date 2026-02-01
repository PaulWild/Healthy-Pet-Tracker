package com.example.healthypettracker.domain.repository

import com.example.healthypettracker.data.local.entity.WeightEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface WeightRepository {
    fun getWeightEntriesForCat(catId: Long): Flow<List<WeightEntry>>
    fun getRecentWeightEntries(catId: Long, limit: Int): Flow<List<WeightEntry>>
    fun getLatestWeightEntry(catId: Long): Flow<WeightEntry?>
    suspend fun getWeightEntryById(entryId: Long): WeightEntry?
    fun getWeightEntriesSince(catId: Long, since: LocalDateTime): Flow<List<WeightEntry>>
    suspend fun insertWeightEntry(entry: WeightEntry): Long
    suspend fun updateWeightEntry(entry: WeightEntry)
    suspend fun deleteWeightEntry(entry: WeightEntry)
    suspend fun deleteWeightEntryById(entryId: Long)
}
