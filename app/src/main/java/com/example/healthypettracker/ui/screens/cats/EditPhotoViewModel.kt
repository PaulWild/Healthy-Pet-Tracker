package com.example.healthypettracker.ui.screens.cats

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthypettracker.domain.repository.CatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class EditPhotoViewModel @Inject constructor(
    private val catRepository: CatRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val catId: Long = savedStateHandle["catId"] ?: -1L

    fun savePhoto(newPhotoUri: Uri) {
        viewModelScope.launch {
            val cat = catRepository.getCatById(catId)
            if (cat != null) {
                catRepository.updateCat(cat.copy(photoUri = newPhotoUri.toString()))
            }
        }
    }

}
