package com.example.healthypettracker.ui.screens.cats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthypettracker.data.local.entity.Cat
import com.example.healthypettracker.data.local.entity.DiaryNote
import com.example.healthypettracker.data.local.entity.Medicine
import com.example.healthypettracker.data.local.entity.WeightEntry
import com.example.healthypettracker.domain.repository.CatRepository
import com.example.healthypettracker.domain.repository.DiaryRepository
import com.example.healthypettracker.domain.repository.MedicineRepository
import com.example.healthypettracker.domain.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CatDetailViewModel @Inject constructor(
    private val catRepository: CatRepository,
    private val medicineRepository: MedicineRepository,
    private val weightRepository: WeightRepository,
    private val diaryRepository: DiaryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val catId: Long = savedStateHandle["catId"] ?: -1L

    val cat: StateFlow<Cat?> = catRepository.getCatByIdFlow(catId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val medicines: StateFlow<List<Medicine>> = medicineRepository.getMedicinesForCat(catId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val latestWeight: StateFlow<WeightEntry?> = weightRepository.getLatestWeightEntry(catId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val recentDiaryNotes: StateFlow<List<DiaryNote>> = diaryRepository.getDiaryNotesForCat(catId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch {
            medicineRepository.deleteMedicine(medicine)
        }
    }
}