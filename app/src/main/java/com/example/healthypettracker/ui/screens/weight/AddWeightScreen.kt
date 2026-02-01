package com.example.healthypettracker.ui.screens.weight

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthypettracker.data.local.entity.WeightEntry
import com.example.healthypettracker.domain.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AddWeightViewModel @Inject constructor(
    private val weightRepository: WeightRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val catId: Long = savedStateHandle["catId"] ?: error("catId required")

    private val _weightKg = MutableStateFlow("")
    val weightKg: StateFlow<String> = _weightKg.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _saveComplete = MutableStateFlow(false)
    val saveComplete: StateFlow<Boolean> = _saveComplete.asStateFlow()

    fun updateWeightKg(weight: String) {
        // Only allow valid decimal numbers
        if (weight.isEmpty() || weight.matches(Regex("^\\d*\\.?\\d*$"))) {
            _weightKg.value = weight
        }
    }

    fun updateNotes(notes: String) {
        _notes.value = notes
    }

    fun save() {
        val weightValue = _weightKg.value.toDoubleOrNull() ?: return
        if (weightValue <= 0) return

        val weightGrams = (weightValue * 1000).toInt()

        viewModelScope.launch {
            val entry = WeightEntry(
                catId = catId,
                weightGrams = weightGrams,
                measuredAt = LocalDateTime.now(),
                notes = _notes.value.trim().ifBlank { null }
            )
            weightRepository.insertWeightEntry(entry)
            _saveComplete.value = true
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWeightScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddWeightViewModel = hiltViewModel()
) {
    val weightKg by viewModel.weightKg.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val saveComplete by viewModel.saveComplete.collectAsState()

    LaunchedEffect(saveComplete) {
        if (saveComplete) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Weight") },
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
            OutlinedTextField(
                value = weightKg,
                onValueChange = { viewModel.updateWeightKg(it) },
                label = { Text("Weight (kg) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("kg") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        }
    }
}
