package com.example.healthypettracker.data.repository

import com.example.healthypettracker.data.local.dao.DiaryDao
import com.example.healthypettracker.data.local.entity.DiaryCategory
import com.example.healthypettracker.data.local.entity.DiaryNote
import com.example.healthypettracker.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class DiaryRepositoryImpl(private val diaryDao: DiaryDao) : DiaryRepository {
    override fun getDiaryNotesForCat(catId: Long): Flow<List<DiaryNote>> =
        diaryDao.getDiaryNotesForCat(catId)

    override fun getDiaryNotesByCategory(catId: Long, category: DiaryCategory): Flow<List<DiaryNote>> =
        diaryDao.getDiaryNotesByCategory(catId, category)

    override fun getDiaryNotesForDateRange(
        catId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<DiaryNote>> =
        diaryDao.getDiaryNotesForDateRange(catId, startDate, endDate)

    override suspend fun getDiaryNoteById(noteId: Long): DiaryNote? =
        diaryDao.getDiaryNoteById(noteId)

    override fun getDiaryNoteByIdFlow(noteId: Long): Flow<DiaryNote?> =
        diaryDao.getDiaryNoteByIdFlow(noteId)

    override suspend fun insertDiaryNote(note: DiaryNote): Long =
        diaryDao.insert(note)

    override suspend fun updateDiaryNote(note: DiaryNote) =
        diaryDao.update(note)

    override suspend fun deleteDiaryNote(note: DiaryNote) =
        diaryDao.delete(note)

    override suspend fun deleteDiaryNoteById(noteId: Long) =
        diaryDao.deleteById(noteId)
}
