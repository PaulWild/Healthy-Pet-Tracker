package com.example.healthypettracker.ui.screens.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.healthypettracker.data.local.entity.FoodEntry
import com.example.healthypettracker.domain.repository.FoodRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FoodLogViewModel(
    private val foodRepository: FoodRepository,
    private val catId: Long
) : ViewModel() {

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

    class Factory(
        private val foodRepository: FoodRepository,
        private val catId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FoodLogViewModel(foodRepository, catId) as T
        }
    }
}