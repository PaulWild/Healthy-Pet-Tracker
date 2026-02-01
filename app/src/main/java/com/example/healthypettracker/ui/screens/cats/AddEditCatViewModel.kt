package com.example.healthypettracker.ui.screens.cats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthypettracker.data.local.entity.Cat
import com.example.healthypettracker.domain.repository.CatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _saveComplete = MutableStateFlow(false)
    val saveComplete: StateFlow<Boolean> = _saveComplete.asStateFlow()

    val isEditMode = catId != null

    init {
        if (catId != null) {
            loadCat(catId)
        }
    }

    private fun loadCat(catId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            catRepository.getCatById(catId)?.let { cat ->
                _name.value = cat.name
                _breed.value = cat.breed ?: ""
                _birthDate.value = cat.birthDate
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

    fun save() {
        if (_name.value.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
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
            _isLoading.value = false
            _saveComplete.value = true
        }
    }
}