package com.example.healthypettracker.ui.screens.cats

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.healthypettracker.domain.repository.CatRepository
import kotlinx.coroutines.launch

class EditPhotoViewModel(
    private val catRepository: CatRepository
) : ViewModel() {

    fun savePhoto(catId: Long, photoUri: Uri) {
        viewModelScope.launch {
            val cat = catRepository.getCatById(catId)
            if (cat != null) {
                catRepository.updateCat(cat.copy(photoUri = photoUri.toString()))
            }
        }
    }

    class Factory(private val catRepository: CatRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EditPhotoViewModel(catRepository) as T
        }
    }
}
