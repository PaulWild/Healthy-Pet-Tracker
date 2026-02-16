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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val cats by viewModel.cats.collectAsState()
    val selectedCatIds by viewModel.selectedCatIds.collectAsState()
    val timelineEntries by viewModel.timelineEntries.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diary") },
                actions = {
                    if (cats.isNotEmpty()) {
                        CatSelectorHeader(
                            cats = cats,
                            selectedCatIds = selectedCatIds,
                            onToggleCat = { viewModel.toggleCatSelection(it) },
                            onSelectAll = { viewModel.selectAllCats() }
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

                            items(
                                entries,
                                key = { "${it::class.simpleName}_${it.dateTime}_${it.catName}" }) { entry ->
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

@Composable
private fun CatSelectorHeader(
    cats: List<Cat>,
    selectedCatIds: Set<Long>,
    onToggleCat: (Long) -> Unit,
    onSelectAll: () -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }
    val displayCats = if (selectedCatIds.isEmpty()) cats else cats.filter { it.id in selectedCatIds }

    Row(verticalAlignment = Alignment.CenterVertically) {
        // Overlapping avatars (50% overlap)
        Box(
            modifier = Modifier.width((32 + (displayCats.size - 1).coerceAtLeast(0) * 16).dp)
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

        // Chevron
        IconButton(onClick = { showDropdown = true }) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Select cats")
        }

        // Multi-select dropdown
        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false }
        ) {
            DropdownMenuItem(
                text = { Text("All cats") },
                onClick = { onSelectAll(); showDropdown = false },
                leadingIcon = {
                    Checkbox(
                        checked = selectedCatIds.isEmpty(),
                        onCheckedChange = null
                    )
                }
            )
            cats.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat.name) },
                    onClick = { onToggleCat(cat.id) },
                    leadingIcon = {
                        Checkbox(
                            checked = cat.id in selectedCatIds,
                            onCheckedChange = null
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SmallCatAvatar(cat: Cat, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.size(32.dp),
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
