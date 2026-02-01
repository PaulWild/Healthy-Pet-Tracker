package com.example.healthypettracker.ui.screens.cats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthypettracker.data.local.entity.Cat
import com.example.healthypettracker.di.AppContainer
import com.example.healthypettracker.domain.repository.CatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AddEditCatViewModel(
    private val catRepository: CatRepository,
    private val catId: Long?
) : ViewModel() {

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

    class Factory(
        private val catRepository: CatRepository,
        private val catId: Long?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddEditCatViewModel(catRepository, catId) as T
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCatScreen(
    container: AppContainer,
    catId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: AddEditCatViewModel = viewModel(
        factory = AddEditCatViewModel.Factory(container.catRepository, catId)
    )
) {
    val name by viewModel.name.collectAsState()
    val breed by viewModel.breed.collectAsState()
    val birthDate by viewModel.birthDate.collectAsState()
    val saveComplete by viewModel.saveComplete.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(saveComplete) {
        if (saveComplete) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditMode) "Edit Cat" else "Add Cat") },
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
            FloatingActionButton(
                onClick = { viewModel.save() }
            ) {
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
                value = name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = breed,
                onValueChange = { viewModel.updateBreed(it) },
                label = { Text("Breed (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = birthDate?.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) ?: "",
                onValueChange = { },
                label = { Text("Birth Date (optional)") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text(if (birthDate != null) "Change" else "Select")
                    }
                }
            )

            if (birthDate != null) {
                TextButton(
                    onClick = { viewModel.updateBirthDate(null) }
                ) {
                    Text("Clear date", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = birthDate?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()?.toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            viewModel.updateBirthDate(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
