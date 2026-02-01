package com.example.healthypettracker.ui.screens.medicine

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthypettracker.data.local.entity.Medicine
import com.example.healthypettracker.domain.repository.MedicineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditMedicineViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val catId: Long? = savedStateHandle["catId"]
    private val medicineId: Long? = savedStateHandle["medicineId"]

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _dosage = MutableStateFlow("")
    val dosage: StateFlow<String> = _dosage.asStateFlow()

    private val _instructions = MutableStateFlow("")
    val instructions: StateFlow<String> = _instructions.asStateFlow()

    private val _isActive = MutableStateFlow(true)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    private val _saveComplete = MutableStateFlow(false)
    val saveComplete: StateFlow<Boolean> = _saveComplete.asStateFlow()

    private var loadedCatId: Long? = catId

    val isEditMode = medicineId != null

    init {
        if (medicineId != null) {
            loadMedicine(medicineId)
        }
    }

    private fun loadMedicine(medicineId: Long) {
        viewModelScope.launch {
            medicineRepository.getMedicineById(medicineId)?.let { medicine ->
                _name.value = medicine.name
                _dosage.value = medicine.dosage ?: ""
                _instructions.value = medicine.instructions ?: ""
                _isActive.value = medicine.isActive
                loadedCatId = medicine.catId
            }
        }
    }

    fun updateName(name: String) {
        _name.value = name
    }

    fun updateDosage(dosage: String) {
        _dosage.value = dosage
    }

    fun updateInstructions(instructions: String) {
        _instructions.value = instructions
    }

    fun updateIsActive(isActive: Boolean) {
        _isActive.value = isActive
    }

    fun save() {
        val currentCatId = loadedCatId ?: return
        if (_name.value.isBlank()) return

        viewModelScope.launch {
            val medicine = Medicine(
                id = medicineId ?: 0,
                catId = currentCatId,
                name = _name.value.trim(),
                dosage = _dosage.value.trim().ifBlank { null },
                instructions = _instructions.value.trim().ifBlank { null },
                isActive = _isActive.value
            )

            if (medicineId != null) {
                medicineRepository.updateMedicine(medicine)
            } else {
                medicineRepository.insertMedicine(medicine)
            }
            _saveComplete.value = true
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicineScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditMedicineViewModel = hiltViewModel()
) {
    val name by viewModel.name.collectAsState()
    val dosage by viewModel.dosage.collectAsState()
    val instructions by viewModel.instructions.collectAsState()
    val isActive by viewModel.isActive.collectAsState()
    val saveComplete by viewModel.saveComplete.collectAsState()

    LaunchedEffect(saveComplete) {
        if (saveComplete) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditMode) "Edit Medicine" else "Add Medicine") },
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
                value = name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Medicine Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = dosage,
                onValueChange = { viewModel.updateDosage(it) },
                label = { Text("Dosage (e.g., \"1 tablet\", \"2ml\")") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = instructions,
                onValueChange = { viewModel.updateInstructions(it) },
                label = { Text("Instructions (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active",
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isActive,
                    onCheckedChange = { viewModel.updateIsActive(it) }
                )
            }
        }
    }
}
