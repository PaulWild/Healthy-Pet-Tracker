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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class LoadedDateRange(
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    fun isNearStart(date: LocalDate, threshold: Int): Boolean =
        date.minusDays(threshold.toLong()) <= startDate

    fun isNearEnd(date: LocalDate, threshold: Int): Boolean =
        date.plusDays(threshold.toLong()) >= endDate
}

private sealed class CatData {
    data class Diary(val notes: List<DiaryNote>) : CatData()
    data class Weight(val entries: List<WeightEntry>) : CatData()
    data class Food(val entries: List<FoodEntry>) : CatData()
}

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val catRepository: CatRepository,
    private val diaryRepository: DiaryRepository,
    private val weightRepository: WeightRepository,
    private val foodRepository: FoodRepository,
    private val medicineRepository: MedicineRepository
) : ViewModel() {

    companion object {
        const val INITIAL_WINDOW_DAYS = 7
        const val EXPANSION_DAYS = 14
        const val EDGE_THRESHOLD_DAYS = 3
    }

    private val _loadedRange = MutableStateFlow(
        LoadedDateRange(
            startDate = LocalDate.now().minusDays(INITIAL_WINDOW_DAYS.toLong()),
            endDate = LocalDate.now().plusDays(INITIAL_WINDOW_DAYS.toLong())
        )
    )

    val cats: StateFlow<List<Cat>> = catRepository.getAllCats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedCatIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedCatIds: StateFlow<Set<Long>> = _selectedCatIds.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private var hasInitializedSelection = false
    private val _catNameMap = MutableStateFlow<Map<Long, String>>(emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    val timelineEntries: StateFlow<List<TimelineEntry>> = combine(
        cats,
        _selectedCatIds,
        _loadedRange
    ) { catsList, selectedIds, loadedRange ->
        _catNameMap.value = catsList.associate { it.id to it.name }
        Triple(selectedIds, catsList, loadedRange)
    }.flatMapLatest { (selectedIds, catsList, loadedRange) ->
        if (catsList.isEmpty()) {
            flowOf(emptyList())
        } else {
            // On first load, select all cats
            val catIds = if (!hasInitializedSelection) {
                hasInitializedSelection = true
                val allCatIds = catsList.map { it.id }.toSet()
                _selectedCatIds.value = allCatIds
                allCatIds.toList()
            } else if (selectedIds.isEmpty()) {
                // User deselected all cats - show no entries
                return@flatMapLatest flowOf(emptyList())
            } else {
                selectedIds.toList()
            }

            // Use dynamic date range from loadedRange
            val rangeStart = LocalDateTime.of(loadedRange.startDate, LocalTime.MIN)
            val rangeEnd = LocalDateTime.of(loadedRange.endDate.plusDays(1), LocalTime.MIN)

            // Create typed flows for each data source with date range filtering at DB level
            val typedFlows: List<Flow<CatData>> = catIds.flatMap { catId ->
                listOf(
                    diaryRepository.getDiaryNotesForDateRange(catId, rangeStart, rangeEnd)
                        .map { CatData.Diary(it) },
                    weightRepository.getWeightEntriesForDateRange(catId, rangeStart, rangeEnd)
                        .map { CatData.Weight(it) },
                    foodRepository.getFoodEntriesForDateRange(catId, rangeStart, rangeEnd)
                        .map { CatData.Food(it) }
                )
            }

            combine(typedFlows) { results ->
                val allEntries = mutableListOf<TimelineEntry>()
                val catNameMap = _catNameMap.value

                results.forEach { data ->
                    when (data) {
                        is CatData.Diary -> data.notes.forEach { note ->
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

                        is CatData.Weight -> data.entries.forEach { entry ->
                            allEntries.add(
                                TimelineEntry.Weight(
                                    dateTime = entry.measuredAt,
                                    catName = catNameMap[entry.catId] ?: "Unknown",
                                    weightGrams = entry.weightGrams,
                                    notes = entry.notes
                                )
                            )
                        }

                        is CatData.Food -> data.entries.forEach { entry ->
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
                }

                // Sort newest first (date filtering already done at DB level)
                allEntries.sortedByDescending { it.dateTime }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleCatSelection(catId: Long) {
        _selectedCatIds.value = if (catId in _selectedCatIds.value) {
            _selectedCatIds.value - catId
        } else {
            _selectedCatIds.value + catId
        }
    }

    fun goToPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun goToNextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }

    fun setDateOffset(daysFromToday: Int) {
        _selectedDate.value = LocalDate.now().plusDays(daysFromToday.toLong())
    }

    fun onDateApproached(date: LocalDate) {
        val currentRange = _loadedRange.value

        if (currentRange.isNearStart(date, EDGE_THRESHOLD_DAYS)) {
            _loadedRange.update { it.copy(startDate = it.startDate.minusDays(EXPANSION_DAYS.toLong())) }
        }

        if (currentRange.isNearEnd(date, EDGE_THRESHOLD_DAYS)) {
            _loadedRange.update { it.copy(endDate = it.endDate.plusDays(EXPANSION_DAYS.toLong())) }
        }
    }

}