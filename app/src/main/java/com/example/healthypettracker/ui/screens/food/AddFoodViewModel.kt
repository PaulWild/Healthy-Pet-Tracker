package com.example.healthypettracker.ui.screens.food

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthypettracker.data.local.entity.FoodEntry
import com.example.healthypettracker.data.local.entity.FoodType
import com.example.healthypettracker.domain.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@HiltViewModel
class AddFoodViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val catId: Long = savedStateHandle["catId"] ?: -1L


    private val _foodType = MutableStateFlow(FoodType.WET)
    val foodType: StateFlow<FoodType> = _foodType.asStateFlow()

    private val _brandName = MutableStateFlow("")
    val brandName: StateFlow<String> = _brandName.asStateFlow()

    private val _amountGrams = MutableStateFlow("")
    val amountGrams: StateFlow<String> = _amountGrams.asStateFlow()

    private val _saveComplete = MutableStateFlow(false)
    val saveComplete: StateFlow<Boolean> = _saveComplete.asStateFlow()

    fun updateFoodType(type: FoodType) {
        _foodType.value = type
    }

    fun updateBrandName(brand: String) {
        _brandName.value = brand
    }

    fun updateAmountGrams(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d*$"))) {
            _amountGrams.value = amount
        }
    }

    fun save() {
        viewModelScope.launch {
            val entry = FoodEntry(
                catId = catId,
                foodType = _foodType.value,
                brandName = _brandName.value.trim().ifBlank { null },
                amountGrams = _amountGrams.value.toIntOrNull(),
                fedAt = LocalDateTime.now()
            )
            foodRepository.insertFoodEntry(entry)
            _saveComplete.value = true
        }
    }

}