package com.example.healthypettracker.ui.screens.diary

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.healthypettracker.data.local.entity.Cat
import com.example.healthypettracker.ui.components.TimelineEntry
import com.example.healthypettracker.ui.components.TimelineItemCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    onNavigateToAddDiaryNote: (Long) -> Unit,
    onNavigateToEditDiaryNote: (Long) -> Unit,
    onNavigateToWeightHistory: (Long) -> Unit,
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val cats by viewModel.cats.collectAsState()
    val selectedCatIds by viewModel.selectedCatIds.collectAsState()
    val timelineEntries by viewModel.timelineEntries.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val today = LocalDate.now()
    val monthTitle = if (selectedDate.year == today.year) {
        selectedDate.format(DateTimeFormatter.ofPattern("MMMM"))
    } else {
        selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(monthTitle) },
                actions = {
                    if (cats.isNotEmpty()) {
                        CatSelectorHeader(
                            cats = cats,
                            selectedCatIds = selectedCatIds,
                            onToggleCat = { viewModel.toggleCatSelection(it) }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (cats.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        val catId = selectedCatIds.firstOrNull() ?: cats.firstOrNull()?.id
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
            // Date display
            DateNavigationControls(
                selectedDate = selectedDate
            )

            HorizontalDivider()

            // HorizontalPager for swipeable day navigation
            if (cats.isEmpty()) {
                // No cats - show empty state without pager
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
                // Use a large initial page to allow swiping both directions
                val initialPage = 10000
                val pagerState = rememberPagerState(initialPage = initialPage) { Int.MAX_VALUE }

                // Sync pager position with selectedDate and trigger dynamic loading
                LaunchedEffect(pagerState) {
                    snapshotFlow { pagerState.currentPage }
                        .collect { page ->
                            val dayOffset = page - initialPage
                            val pageDate = today.plusDays(dayOffset.toLong())
                            viewModel.setDateOffset(dayOffset)
                            viewModel.onDateApproached(pageDate)
                        }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val pageDate = today.plusDays((page - initialPage).toLong())
                    // Filter entries for this specific page's date
                    val pageEntries = remember(timelineEntries, pageDate) {
                        timelineEntries.filter { it.dateTime.toLocalDate() == pageDate }
                    }
                    DiaryDayContent(
                        date = pageDate,
                        timelineEntries = pageEntries,
                        onNavigateToEditDiaryNote = onNavigateToEditDiaryNote,
                        onNavigateToWeightHistory = onNavigateToWeightHistory
                    )
                }
            }
        }
    }
}

@Composable
private fun DiaryDayContent(
    date: LocalDate,
    timelineEntries: List<TimelineEntry>,
    onNavigateToEditDiaryNote: (Long) -> Unit,
    onNavigateToWeightHistory: (Long) -> Unit
) {
    val today = LocalDate.now()

    if (timelineEntries.isEmpty()) {
        val emptyStateText = when (date) {
            today -> "No entries for today"
            today.minusDays(1) -> "No entries for yesterday"
            today.plusDays(1) -> "No entries for tomorrow"
            else -> "No entries for this day"
        }
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
                    text = emptyStateText,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Swipe to navigate between days",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                timelineEntries,
                key = { "${it::class.simpleName}_${it.dateTime}_${it.catName}" }
            ) { entry ->
                TimelineItemCard(
                    entry = entry,
                    onClick = when (entry) {
                        is TimelineEntry.Diary -> {{ onNavigateToEditDiaryNote(entry.noteId) }}
                        is TimelineEntry.Weight -> {{ onNavigateToWeightHistory(entry.catId) }}
                        else -> null
                    }
                )
            }
        }
    }
}

@Composable
private fun DateNavigationControls(
    selectedDate: LocalDate
) {
    // Format: "Mon 16" - 3-letter day abbreviation + day of month
    val dateText = selectedDate.format(DateTimeFormatter.ofPattern("EEE d"))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateText,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatSelectorHeader(
    cats: List<Cat>,
    selectedCatIds: Set<Long>,
    onToggleCat: (Long) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    // Show all cats when all are selected, otherwise show only selected
    val displayCats = if (selectedCatIds.size == cats.size) cats else cats.filter { it.id in selectedCatIds }

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (displayCats.isEmpty()) {
            // Show "None" when no cats selected
            Text(
                text = "None",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable { showSheet = true }
            )
        } else {
            // Overlapping avatars (50% overlap) - clickable to open sheet
            Box(
                modifier = Modifier
                    .width((32 + (displayCats.size - 1).coerceAtLeast(0) * 16).dp)
                    .clickable { showSheet = true }
            ) {
                displayCats.forEachIndexed { index, cat ->
                    SmallCatAvatar(
                        cat = cat,
                        modifier = Modifier
                            .offset(x = (index * 16).dp)
                            .zIndex((displayCats.size - index).toFloat())
                    )
                }
            }
        }

        // Chevron opens sheet
        IconButton(onClick = { showSheet = true }) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Select cats")
        }
    }

    // Bottom sheet with cat cards
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            CatSelectorSheetContent(
                cats = cats,
                selectedCatIds = selectedCatIds,
                onToggleCat = onToggleCat
            )
        }
    }
}

@Composable
private fun CatSelectorSheetContent(
    cats: List<Cat>,
    selectedCatIds: Set<Long>,
    onToggleCat: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // List of cat cards only - no "All Cats" option
        cats.forEach { cat ->
            SelectableCatCard(
                cat = cat,
                isSelected = cat.id in selectedCatIds,
                onClick = { onToggleCat(cat.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Bottom padding for gesture area
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SelectableCatCard(
    cat: Cat,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val glowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier
                        .shadow(
                            elevation = 8.dp,
                            shape = MaterialTheme.shapes.medium,
                            ambientColor = glowColor,
                            spotColor = glowColor
                        )
                        .border(
                            width = 2.dp,
                            color = borderColor,
                            shape = MaterialTheme.shapes.medium
                        )
                } else Modifier
            )
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallCatAvatar(cat = cat, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = cat.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                cat.breed?.let { breed ->
                    Text(
                        text = breed,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallCatAvatar(
    cat: Cat,
    modifier: Modifier = Modifier,
    defaultSize: Int = 32
) {
    Card(
        modifier = modifier.size(defaultSize.dp),
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        if (cat.photoUri != null) {
            AsyncImage(
                model = cat.photoUri.toUri(),
                contentDescription = "${cat.name} photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = cat.name.take(1).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
