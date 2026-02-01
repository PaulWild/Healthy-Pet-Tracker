package com.example.healthypettracker.ui.screens.cats

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthypettracker.data.local.entity.Cat
import com.example.healthypettracker.domain.repository.CatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CatListViewModel @Inject constructor(private val catRepository: CatRepository) : ViewModel() {

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

    fun addCatImage(catId: Long, uri: Uri) {
        viewModelScope.launch {
            val cat = catRepository.getCatById(catId)
            if (cat != null) {
                val updatedCat = cat.copy(photoUri = uri.toString())
                catRepository.updateCat(updatedCat)
            }
        }
    }
}
