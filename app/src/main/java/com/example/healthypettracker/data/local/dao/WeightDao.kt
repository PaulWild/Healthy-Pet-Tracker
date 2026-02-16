package com.example.healthypettracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.healthypettracker.data.local.entity.WeightEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_entries WHERE catId = :catId ORDER BY measuredAt DESC")
    fun getWeightEntriesForCat(catId: Long): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries WHERE catId = :catId ORDER BY measuredAt DESC LIMIT :limit")
    fun getRecentWeightEntries(catId: Long, limit: Int): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries WHERE catId = :catId ORDER BY measuredAt DESC LIMIT 1")
    fun getLatestWeightEntry(catId: Long): Flow<WeightEntry?>

    @Query("SELECT * FROM weight_entries WHERE id = :entryId")
    suspend fun getWeightEntryById(entryId: Long): WeightEntry?

    @Query("SELECT * FROM weight_entries WHERE catId = :catId AND measuredAt >= :since ORDER BY measuredAt ASC")
    fun getWeightEntriesSince(catId: Long, since: LocalDateTime): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries WHERE catId = :catId AND measuredAt >= :startDate AND measuredAt < :endDate ORDER BY measuredAt DESC")
    fun getWeightEntriesForDateRange(catId: Long, startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<WeightEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WeightEntry): Long

    @Update
    suspend fun update(entry: WeightEntry)

    @Delete
    suspend fun delete(entry: WeightEntry)

    @Query("DELETE FROM weight_entries WHERE id = :entryId")
    suspend fun deleteById(entryId: Long)
}
