package com.example.healthypettracker.ui.screens.cats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.healthypettracker.data.local.entity.Cat
import com.example.healthypettracker.domain.repository.CatRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CatListViewModel(
    private val catRepository: CatRepository
) : ViewModel() {

    val cats: StateFlow<List<Cat>> = catRepository.getAllCats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteCat(cat: Cat) {
        viewModelScope.launch {
            catRepository.deleteCat(cat)
        }
    }

    class Factory(private val catRepository: CatRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CatListViewModel(catRepository) as T
        }
    }
}
