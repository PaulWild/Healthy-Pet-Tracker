package com.example.healthypettracker.ui.screens.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthypettracker.data.local.entity.Cat
import com.example.healthypettracker.data.local.entity.DiaryNote
import com.example.healthypettracker.data.local.entity.FoodEntry
import com.example.healthypettracker.data.local.entity.WeightEntry
import com.example.healthypettracker.domain.repository.CatRepository
import com.example.healthypettracker.domain.repository.DiaryRepository
import com.example.healthypettracker.domain.repository.FoodRepository
import com.example.healthypettracker.domain.repository.MedicineRepository
import com.example.healthypettracker.domain.repository.WeightRepository
import com.example.healthypettracker.ui.components.TimelineEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DiaryViewModel @Inject constructor(
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


}