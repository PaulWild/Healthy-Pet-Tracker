package com.example.healthypettracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.healthypettracker.data.local.entity.Cat
import kotlinx.coroutines.flow.Flow

@Dao
interface CatDao {
    @Query("SELECT * FROM cats ORDER BY name ASC")
    fun getAllCats(): Flow<List<Cat>>

    @Query("SELECT * FROM cats WHERE id = :catId")
    suspend fun getCatById(catId: Long): Cat?

    @Query("SELECT * FROM cats WHERE id = :catId")
    fun getCatByIdFlow(catId: Long): Flow<Cat?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cat: Cat): Long

    @Update
    suspend fun update(cat: Cat)

    @Delete
    suspend fun delete(cat: Cat)

    @Query("DELETE FROM cats WHERE id = :catId")
    suspend fun deleteById(catId: Long)
}
