package com.example.healthypettracker.ui.screens.food

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthypettracker.data.local.entity.FoodEntry
import com.example.healthypettracker.domain.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodLogViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val catId: Long = savedStateHandle["catId"] ?: error("catId required")

    val foodEntries: StateFlow<List<FoodEntry>> = foodRepository.getFoodEntriesForCat(catId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteEntry(entry: FoodEntry) {
        viewModelScope.launch {
            foodRepository.deleteFoodEntry(entry)
        }
    }
}