package com.example.healthypettracker.ui.screens.diary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthypettracker.data.local.entity.Cat
import com.example.healthypettracker.data.local.entity.DiaryNote
import com.example.healthypettracker.data.local.entity.FoodEntry
import com.example.healthypettracker.data.local.entity.MedicineLog
import com.example.healthypettracker.data.local.entity.WeightEntry
import com.example.healthypettracker.di.AppContainer
import com.example.healthypettracker.domain.repository.CatRepository
import com.example.healthypettracker.domain.repository.DiaryRepository
import com.example.healthypettracker.domain.repository.FoodRepository
import com.example.healthypettracker.domain.repository.MedicineRepository
import com.example.healthypettracker.domain.repository.WeightRepository
import com.example.healthypettracker.ui.components.TimelineEntry
import com.example.healthypettracker.ui.components.TimelineItemCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DiaryViewModel(
    private val catRepository: CatRepository,
    private val diaryRepository: DiaryRepository,
    private val weightRepository: WeightRepository,
    private val foodRepository: FoodRepository,
    private val medicineRepository: MedicineRepository
) : ViewModel() {

    val cats: StateFlow<List<Cat>> = catRepository.getAllCats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedCatId = MutableStateFlow<Long?>(null)
    val selectedCatId: StateFlow<Long?> = _selectedCatId.asStateFlow()

    private val _catNameMap = MutableStateFlow<Map<Long, String>>(emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    val timelineEntries: StateFlow<List<TimelineEntry>> = combine(
        cats,
        _selectedCatId
    ) { catsList, selectedId ->
        _catNameMap.value = catsList.associate { it.id to it.name }
        selectedId to catsList
    }.flatMapLatest { (selectedId, catsList) ->
        if (catsList.isEmpty()) {
            flowOf(emptyList())
        } else {
            val catIds = if (selectedId != null) listOf(selectedId) else catsList.map { it.id }

            // Combine flows from all selected cats
            val diaryFlows = catIds.map { catId -> diaryRepository.getDiaryNotesForCat(catId) }
            val weightFlows = catIds.map { catId -> weightRepository.getWeightEntriesForCat(catId) }
            val foodFlows = catIds.map { catId -> foodRepository.getFoodEntriesForCat(catId) }

            combine(
                diaryFlows + weightFlows + foodFlows
            ) { results ->
                val allEntries = mutableListOf<TimelineEntry>()
                val catNameMap = _catNameMap.value

                var index = 0
                // Process diary notes
                repeat(catIds.size) {
                    @Suppress("UNCHECKED_CAST")
                    val notes = results[index++] as List<DiaryNote>
                    notes.forEach { note ->
                        allEntries.add(
                            TimelineEntry.Diary(
                                dateTime = note.createdAt,
                                catName = catNameMap[note.catId] ?: "Unknown",
                                title = note.title,
                                content = note.content,
                                category = note.category.name.lowercase().replace('_', ' ')
                                    .replaceFirstChar { it.uppercase() },
                                noteId = note.id
                            )
                        )
                    }
                }

                // Process weight entries
                repeat(catIds.size) {
                    @Suppress("UNCHECKED_CAST")
                    val weights = results[index++] as List<WeightEntry>
                    weights.forEach { entry ->
                        allEntries.add(
                            TimelineEntry.Weight(
                                dateTime = entry.measuredAt,
                                catName = catNameMap[entry.catId] ?: "Unknown",
                                weightGrams = entry.weightGrams,
                                notes = entry.notes
                            )
                        )
                    }
                }

                // Process food entries
                repeat(catIds.size) {
                    @Suppress("UNCHECKED_CAST")
                    val foods = results[index++] as List<FoodEntry>
                    foods.forEach { entry ->
                        allEntries.add(
                            TimelineEntry.Food(
                                dateTime = entry.fedAt,
                                catName = catNameMap[entry.catId] ?: "Unknown",
                                foodType = entry.foodType.name.lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                brandName = entry.brandName,
                                amountGrams = entry.amountGrams
                            )
                        )
                    }
                }

                // Sort by date, newest first
                allEntries.sortedByDescending { it.dateTime }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectCat(catId: Long?) {
        _selectedCatId.value = catId
    }

    class Factory(
        private val catRepository: CatRepository,
        private val diaryRepository: DiaryRepository,
        private val weightRepository: WeightRepository,
        private val foodRepository: FoodRepository,
        private val medicineRepository: MedicineRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DiaryViewModel(
                catRepository,
                diaryRepository,
                weightRepository,
                foodRepository,
                medicineRepository
            ) as T
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    container: AppContainer,
    onNavigateToAddDiaryNote: (Long) -> Unit,
    onNavigateToEditDiaryNote: (Long) -> Unit,
    viewModel: DiaryViewModel = viewModel(
        factory = DiaryViewModel.Factory(
            container.catRepository,
            container.diaryRepository,
            container.weightRepository,
            container.foodRepository,
            container.medicineRepository
        )
    )
) {
    val cats by viewModel.cats.collectAsState()
    val selectedCatId by viewModel.selectedCatId.collectAsState()
    val timelineEntries by viewModel.timelineEntries.collectAsState()

    var showCatDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diary") }
            )
        },
        floatingActionButton = {
            if (cats.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        val catId = selectedCatId ?: cats.firstOrNull()?.id
                        catId?.let { onNavigateToAddDiaryNote(it) }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add note"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (cats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No cats yet",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add a cat first to start tracking",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Cat selector
                ExposedDropdownMenuBox(
                    expanded = showCatDropdown,
                    onExpandedChange = { showCatDropdown = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = selectedCatId?.let { id -> cats.find { it.id == id }?.name }
                            ?: "All cats",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCatDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )

                    ExposedDropdownMenu(
                        expanded = showCatDropdown,
                        onDismissRequest = { showCatDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All cats") },
                            onClick = {
                                viewModel.selectCat(null)
                                showCatDropdown = false
                            }
                        )
                        cats.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    viewModel.selectCat(cat.id)
                                    showCatDropdown = false
                                }
                            )
                        }
                    }
                }

                if (timelineEntries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No entries yet",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your timeline will show all activities",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Group entries by date
                    val groupedEntries = timelineEntries.groupBy { it.dateTime.toLocalDate() }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        groupedEntries.forEach { (date, entries) ->
                            item {
                                DateHeader(date = date)
                            }

                            items(entries, key = { "${it::class.simpleName}_${it.dateTime}_${it.catName}" }) { entry ->
                                TimelineItemCard(
                                    entry = entry,
                                    onClick = if (entry is TimelineEntry.Diary) {
                                        { onNavigateToEditDiaryNote(entry.noteId) }
                                    } else null
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateHeader(date: LocalDate) {
    val today = LocalDate.now()
    val dateText = when {
        date == today -> "Today"
        date == today.minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
    }

    Text(
        text = dateText,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
