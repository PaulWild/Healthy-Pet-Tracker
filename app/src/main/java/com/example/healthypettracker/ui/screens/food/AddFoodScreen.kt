package com.example.healthypettracker.ui.screens.food

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthypettracker.data.local.entity.FoodEntry
import com.example.healthypettracker.data.local.entity.FoodType
import com.example.healthypettracker.di.AppContainer
import com.example.healthypettracker.domain.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AddFoodViewModel(
    private val foodRepository: FoodRepository,
    private val catId: Long
) : ViewModel() {

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

    class Factory(
        private val foodRepository: FoodRepository,
        private val catId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddFoodViewModel(foodRepository, catId) as T
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    container: AppContainer,
    catId: Long,
    onNavigateBack: () -> Unit,
    viewModel: AddFoodViewModel = viewModel(
        factory = AddFoodViewModel.Factory(container.foodRepository, catId)
    )
) {
    val foodType by viewModel.foodType.collectAsState()
    val brandName by viewModel.brandName.collectAsState()
    val amountGrams by viewModel.amountGrams.collectAsState()
    val saveComplete by viewModel.saveComplete.collectAsState()

    LaunchedEffect(saveComplete) {
        if (saveComplete) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Food") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.save() }) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Food Type",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.selectableGroup()) {
                FoodType.entries.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = foodType == type,
                                onClick = { viewModel.updateFoodType(type) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = foodType == type,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = brandName,
                onValueChange = { viewModel.updateBrandName(it) },
                label = { Text("Brand Name (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amountGrams,
                onValueChange = { viewModel.updateAmountGrams(it) },
                label = { Text("Amount (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = { Text("grams") }
            )
        }
    }
}
