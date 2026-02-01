package com.example.healthypettracker.ui.screens.cats

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.healthypettracker.domain.repository.CatRepository
import kotlinx.coroutines.launch

class EditPhotoViewModel(
    val catId: Long,
    private val catRepository: CatRepository
) : ViewModel() {

    fun savePhoto(newPhotoUri: Uri) {
        viewModelScope.launch {
            val cat = catRepository.getCatById(catId)
            if (cat != null) {
                catRepository.updateCat(cat.copy(photoUri = newPhotoUri.toString()))
            }
        }
    }

    class Factory(
        private val catId: Long,
        private val catRepository: CatRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EditPhotoViewModel(catId, catRepository) as T
        }
    }
}
