package com.example.healthypettracker.ui.screens.diary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthypettracker.data.local.entity.DiaryCategory
import com.example.healthypettracker.data.local.entity.DiaryNote
import com.example.healthypettracker.domain.repository.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@HiltViewModel
class AddDiaryNoteViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val catId: Long? = savedStateHandle["catId"]
    val noteId: Long? = savedStateHandle["noteId"]

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _category = MutableStateFlow(DiaryCategory.GENERAL)
    val category: StateFlow<DiaryCategory> = _category.asStateFlow()

    private val _saveComplete = MutableStateFlow(false)
    val saveComplete: StateFlow<Boolean> = _saveComplete.asStateFlow()

    private var loadedCatId: Long? = catId
    private var originalCreatedAt: LocalDateTime? = null

    val isEditMode = noteId != null

    init {
        if (noteId != null) {
            loadNote(noteId)
        }
    }

    private fun loadNote(noteId: Long) {
        viewModelScope.launch {
            diaryRepository.getDiaryNoteById(noteId)?.let { note ->
                _title.value = note.title
                _content.value = note.content ?: ""
                _category.value = note.category
                loadedCatId = note.catId
                originalCreatedAt = note.createdAt
            }
        }
    }

    fun updateTitle(title: String) {
        _title.value = title
    }

    fun updateContent(content: String) {
        _content.value = content
    }

    fun updateCategory(category: DiaryCategory) {
        _category.value = category
    }

    fun save() {
        val currentCatId = loadedCatId ?: return
        if (_title.value.isBlank()) return

        viewModelScope.launch {
            val note = DiaryNote(
                id = noteId ?: 0,
                catId = currentCatId,
                title = _title.value.trim(),
                content = _content.value.trim().ifBlank { null },
                category = _category.value,
                createdAt = originalCreatedAt ?: LocalDateTime.now()
            )

            if (noteId != null) {
                diaryRepository.updateDiaryNote(note)
            } else {
                diaryRepository.insertDiaryNote(note)
            }
            _saveComplete.value = true
        }
    }
}