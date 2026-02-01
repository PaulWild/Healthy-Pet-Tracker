package com.example.healthypettracker.ui.screens.cats

import android.net.Uri
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthypettracker.data.local.entity.Cat
import com.example.healthypettracker.data.local.entity.DiaryNote
import com.example.healthypettracker.data.local.entity.Medicine
import com.example.healthypettracker.data.local.entity.WeightEntry
import com.example.healthypettracker.ui.components.CatAvatar
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditCat: () -> Unit,
    onNavigateToAddMedicine: () -> Unit,
    onNavigateToEditMedicine: (Long) -> Unit,
    onNavigateToMedicineSchedule: (Long) -> Unit,
    onNavigateToAddWeight: () -> Unit,
    onNavigateToWeightHistory: () -> Unit,
    onNavigateToAddFood: () -> Unit,
    onNavigateToFoodLog: () -> Unit,
    onNavigateToAddDiaryNote: () -> Unit,
    onNavigateToEditPhoto: (Long, Uri) -> Unit,
    viewModel: CatDetailViewModel = hiltViewModel()


) {
    val cat by viewModel.cat.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Medicine", "Diary")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cat?.name ?: "Cat Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEditCat) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit cat"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> OverviewTab(
                    viewModel = viewModel,
                    onNavigateToAddWeight = onNavigateToAddWeight,
                    onNavigateToWeightHistory = onNavigateToWeightHistory,
                    onNavigateToAddFood = onNavigateToAddFood,
                    onNavigateToFoodLog = onNavigateToFoodLog,
                    onNavigateToEditPhoto = onNavigateToEditPhoto
                )

                1 -> MedicineTab(
                    viewModel = viewModel,
                    onNavigateToAddMedicine = onNavigateToAddMedicine,
                    onNavigateToEditMedicine = onNavigateToEditMedicine,
                    onNavigateToMedicineSchedule = onNavigateToMedicineSchedule
                )

                2 -> DiaryTab(
                    viewModel = viewModel,
                    onNavigateToAddDiaryNote = onNavigateToAddDiaryNote
                )
            }
        }
    }
}

@Composable
private fun OverviewTab(
    viewModel: CatDetailViewModel,
    onNavigateToAddWeight: () -> Unit,
    onNavigateToWeightHistory: () -> Unit,
    onNavigateToAddFood: () -> Unit,
    onNavigateToFoodLog: () -> Unit,
    onNavigateToEditPhoto: (Long, Uri) -> Unit
) {
    val cat by viewModel.cat.collectAsState()
    val latestWeight by viewModel.latestWeight.collectAsState()
    val picker = rememberPhotoPickerController { catId, uri -> onNavigateToEditPhoto(catId, uri) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            cat?.let { CatInfoCard(cat = it, onClick = { picker.pickFor(it.id) }) }
        }

        item {
            WeightSummaryCard(
                latestWeight = latestWeight,
                onAddWeight = onNavigateToAddWeight,
                onViewHistory = onNavigateToWeightHistory
            )
        }

        item {
            FoodQuickActions(
                onAddFood = onNavigateToAddFood,
                onViewLog = onNavigateToFoodLog
            )
        }
    }
}

@Composable
private fun CatInfoCard(cat: Cat, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CatAvatar(
                name = cat.name,
                modifier = Modifier.size(72.dp),
                photoUri = cat.photoUri?.toUri(),
                onClick = onClick
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = cat.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                cat.breed?.let { breed ->
                    Text(
                        text = breed,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                cat.birthDate?.let { birthDate ->
                    Text(
                        text = "Born: ${birthDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun WeightSummaryCard(
    latestWeight: WeightEntry?,
    onAddWeight: () -> Unit,
    onViewHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Weight",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (latestWeight != null) {
                val weightKg = latestWeight.weightGrams / 1000.0
                Text(
                    text = String.format("%.2f kg", weightKg),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Last recorded: ${
                        latestWeight.measuredAt.format(
                            DateTimeFormatter.ofPattern(
                                "MMM d, yyyy"
                            )
                        )
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "No weight recorded yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAddWeight,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
                OutlinedButton(
                    onClick = onViewHistory,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("History")
                }
            }
        }
    }
}

@Composable
private fun FoodQuickActions(
    onAddFood: () -> Unit,
    onViewLog: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Food",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAddFood,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Food")
                }
                OutlinedButton(
                    onClick = onViewLog,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("View Log")
                }
            }
        }
    }
}

@Composable
private fun MedicineTab(
    viewModel: CatDetailViewModel,
    onNavigateToAddMedicine: () -> Unit,
    onNavigateToEditMedicine: (Long) -> Unit,
    onNavigateToMedicineSchedule: (Long) -> Unit
) {
    val medicines by viewModel.medicines.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (medicines.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No medicines",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add medicines to track and schedule reminders",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = onNavigateToAddMedicine) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Medicine")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedButton(
                        onClick = onNavigateToAddMedicine,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Medicine")
                    }
                }

                items(medicines, key = { it.id }) { medicine ->
                    MedicineCard(
                        medicine = medicine,
                        onEdit = { onNavigateToEditMedicine(medicine.id) },
                        onSchedule = { onNavigateToMedicineSchedule(medicine.id) },
                        onDelete = { viewModel.deleteMedicine(medicine) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicineCard(
    medicine: Medicine,
    onEdit: () -> Unit,
    onSchedule: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medicine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    medicine.dosage?.let { dosage ->
                        Text(
                            text = dosage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!medicine.isActive) {
                    Text(
                        text = "Inactive",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            medicine.instructions?.let { instructions ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = instructions,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onSchedule) {
                    Text("Schedule")
                }
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun DiaryTab(
    viewModel: CatDetailViewModel,
    onNavigateToAddDiaryNote: () -> Unit
) {
    val diaryNotes by viewModel.recentDiaryNotes.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (diaryNotes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No diary entries",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add notes about your cat's health, behavior, and more",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = onNavigateToAddDiaryNote) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Note")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedButton(
                        onClick = onNavigateToAddDiaryNote,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Note")
                    }
                }

                items(diaryNotes.take(10), key = { it.id }) { note ->
                    DiaryNoteCard(note = note)
                }
            }
        }
    }
}

@Composable
private fun DiaryNoteCard(note: DiaryNote) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = note.category.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = note.createdAt.format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            note.content?.let { content ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3
                )
            }
        }
    }
}
