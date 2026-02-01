package com.example.healthypettracker.ui.screens.food

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthypettracker.data.local.entity.FoodEntry
import com.example.healthypettracker.data.local.entity.FoodType
import com.example.healthypettracker.domain.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@HiltViewModel
class AddFoodViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val catId: Long? = savedStateHandle["catId"]

    private val _foodType = MutableStateFlow(FoodType.WET)
    val foodType: StateFlow<FoodType> = _foodType.asStateFlow()

    private val _brandName = MutableStateFlow("")
    val brandName: StateFlow<String> = _brandName.asStateFlow()

    private val _amountGrams = MutableStateFlow("")
    val amountGrams: StateFlow<String> = _amountGrams.asStateFlow()

    private val _saveComplete = Channel<Unit>(Channel.BUFFERED)
    val saveComplete = _saveComplete.receiveAsFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val isMissingCatId: Boolean = catId == null

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

    fun clearError() {
        _error.value = null
    }

    fun save() {
        if (catId == null) {
            _error.value = "Unable to save: missing cat information"
            return
        }

        viewModelScope.launch {
            try {
                val entry = FoodEntry(
                    catId = catId,
                    foodType = _foodType.value,
                    brandName = _brandName.value.trim().ifBlank { null },
                    amountGrams = _amountGrams.value.toIntOrNull(),
                    fedAt = LocalDateTime.now()
                )
                foodRepository.insertFoodEntry(entry)
                _saveComplete.send(Unit)
            } catch (e: Exception) {
                _error.value = "Failed to save: ${e.message}"
            }
        }
    }
}