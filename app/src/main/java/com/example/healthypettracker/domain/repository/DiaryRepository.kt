package com.example.healthypettracker.domain.repository

import com.example.healthypettracker.data.local.entity.DiaryCategory
import com.example.healthypettracker.data.local.entity.DiaryNote
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface DiaryRepository {
    fun getDiaryNotesForCat(catId: Long): Flow<List<DiaryNote>>
    fun getDiaryNotesByCategory(catId: Long, category: DiaryCategory): Flow<List<DiaryNote>>
    fun getDiaryNotesForDateRange(catId: Long, startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<DiaryNote>>
    suspend fun getDiaryNoteById(noteId: Long): DiaryNote?
    fun getDiaryNoteByIdFlow(noteId: Long): Flow<DiaryNote?>
    suspend fun insertDiaryNote(note: DiaryNote): Long
    suspend fun updateDiaryNote(note: DiaryNote)
    suspend fun deleteDiaryNote(note: DiaryNote)
    suspend fun deleteDiaryNoteById(noteId: Long)
}
