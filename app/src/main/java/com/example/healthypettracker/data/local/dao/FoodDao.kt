package com.example.healthypettracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.healthypettracker.data.local.entity.FoodEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_entries WHERE catId = :catId ORDER BY fedAt DESC")
    fun getFoodEntriesForCat(catId: Long): Flow<List<FoodEntry>>

    @Query("SELECT * FROM food_entries WHERE catId = :catId AND fedAt >= :startOfDay AND fedAt < :endOfDay ORDER BY fedAt DESC")
    fun getFoodEntriesForDay(catId: Long, startOfDay: LocalDateTime, endOfDay: LocalDateTime): Flow<List<FoodEntry>>

    @Query("SELECT * FROM food_entries WHERE id = :entryId")
    suspend fun getFoodEntryById(entryId: Long): FoodEntry?

    @Query("SELECT * FROM food_entries WHERE catId = :catId ORDER BY fedAt DESC LIMIT :limit")
    fun getRecentFoodEntries(catId: Long, limit: Int): Flow<List<FoodEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: FoodEntry): Long

    @Update
    suspend fun update(entry: FoodEntry)

    @Delete
    suspend fun delete(entry: FoodEntry)

    @Query("DELETE FROM food_entries WHERE id = :entryId")
    suspend fun deleteById(entryId: Long)
}
