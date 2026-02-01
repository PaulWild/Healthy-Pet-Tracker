package com.example.healthypettracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.healthypettracker.data.local.entity.DiaryCategory
import com.example.healthypettracker.data.local.entity.DiaryNote
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_notes WHERE catId = :catId ORDER BY createdAt DESC")
    fun getDiaryNotesForCat(catId: Long): Flow<List<DiaryNote>>

    @Query("SELECT * FROM diary_notes WHERE catId = :catId AND category = :category ORDER BY createdAt DESC")
    fun getDiaryNotesByCategory(catId: Long, category: DiaryCategory): Flow<List<DiaryNote>>

    @Query("SELECT * FROM diary_notes WHERE catId = :catId AND createdAt >= :startDate AND createdAt < :endDate ORDER BY createdAt DESC")
    fun getDiaryNotesForDateRange(catId: Long, startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<DiaryNote>>

    @Query("SELECT * FROM diary_notes WHERE id = :noteId")
    suspend fun getDiaryNoteById(noteId: Long): DiaryNote?

    @Query("SELECT * FROM diary_notes WHERE id = :noteId")
    fun getDiaryNoteByIdFlow(noteId: Long): Flow<DiaryNote?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: DiaryNote): Long

    @Update
    suspend fun update(note: DiaryNote)

    @Delete
    suspend fun delete(note: DiaryNote)

    @Query("DELETE FROM diary_notes WHERE id = :noteId")
    suspend fun deleteById(noteId: Long)
}
