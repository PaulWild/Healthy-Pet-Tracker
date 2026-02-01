package com.example.healthypettracker.ui.screens.cats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthypettracker.data.local.entity.Cat
import com.example.healthypettracker.domain.repository.CatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

@HiltViewModel
class AddEditCatViewModel @Inject constructor(
    private val catRepository: CatRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val catId: Long? = savedStateHandle["catId"]

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _breed = MutableStateFlow("")
    val breed: StateFlow<String> = _breed.asStateFlow()

    private val _birthDate = MutableStateFlow<LocalDate?>(null)
    val birthDate: StateFlow<LocalDate?> = _birthDate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveComplete = Channel<Unit>(Channel.BUFFERED)
    val saveComplete = _saveComplete.receiveAsFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val isEditMode = catId != null

    init {
        if (catId != null) {
            loadCat(catId)
        }
    }

    private fun loadCat(catId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                catRepository.getCatById(catId)?.let { cat ->
                    _name.value = cat.name
                    _breed.value = cat.breed ?: ""
                    _birthDate.value = cat.birthDate
                }
            } catch (e: Exception) {
                _error.value = "Failed to load cat: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun updateName(name: String) {
        _name.value = name
    }

    fun updateBreed(breed: String) {
        _breed.value = breed
    }

    fun updateBirthDate(date: LocalDate?) {
        _birthDate.value = date
    }

    fun clearError() {
        _error.value = null
    }

    fun save() {
        if (_name.value.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val cat = Cat(
                    id = catId ?: 0,
                    name = _name.value.trim(),
                    breed = _breed.value.trim().ifBlank { null },
                    birthDate = _birthDate.value
                )

                if (catId != null) {
                    catRepository.updateCat(cat)
                } else {
                    catRepository.insertCat(cat)
                }
                _saveComplete.send(Unit)
            } catch (e: Exception) {
                _error.value = "Failed to save: ${e.message}"
            }
            _isLoading.value = false
        }
    }
}