package com.example.healthypettracker.ui.screens.medicine

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthypettracker.data.local.entity.Medicine
import com.example.healthypettracker.data.local.entity.MedicineSchedule
import com.example.healthypettracker.domain.repository.CatRepository
import com.example.healthypettracker.domain.repository.MedicineRepository
import com.example.healthypettracker.notification.MedicineAlarmScheduler
import com.example.healthypettracker.notification.PermissionHelper
import com.example.healthypettracker.ui.components.DayOfWeekSelector
import com.example.healthypettracker.ui.components.getDayNames
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MedicineScheduleViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository,
    private val catRepository: CatRepository,
    private val alarmScheduler: MedicineAlarmScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val medicineId: Long = savedStateHandle["medicineId"] ?: error("medicineId required")

    val medicine: StateFlow<Medicine?> = medicineRepository.getMedicineByIdFlow(medicineId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val schedules: StateFlow<List<MedicineSchedule>> =
        medicineRepository.getSchedulesForMedicine(medicineId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun addSchedule(time: LocalTime, daysOfWeek: Int) {
        viewModelScope.launch {
            val schedule = MedicineSchedule(
                medicineId = medicineId,
                scheduledTime = time,
                daysOfWeek = daysOfWeek
            )
            val scheduleId = medicineRepository.insertSchedule(schedule)

            // Schedule the alarm
            val med = medicineRepository.getMedicineById(medicineId)
            val cat = med?.let { catRepository.getCatById(it.catId) }
            if (med != null && med.isActive) {
                alarmScheduler.scheduleAlarm(
                    schedule.copy(id = scheduleId),
                    med.name,
                    cat?.name ?: "your cat",
                    med.dosage
                )
            }
        }
    }

    fun deleteSchedule(schedule: MedicineSchedule) {
        viewModelScope.launch {
            alarmScheduler.cancelAlarm(schedule.id)
            medicineRepository.deleteSchedule(schedule)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScheduleScreen(
    onNavigateBack: () -> Unit,
    viewModel: MedicineScheduleViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val medicine by viewModel.medicine.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showExactAlarmDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var pendingSchedule by remember { mutableStateOf<Pair<LocalTime, Int>?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingSchedule?.let { (time, days) ->
                viewModel.addSchedule(time, days)
                pendingSchedule = null
            }
        } else {
            showNotificationDialog = true
        }
    }

    fun tryAddSchedule(time: LocalTime, days: Int) {
        // Check exact alarm permission first
        if (!PermissionHelper.hasExactAlarmPermission(context)) {
            pendingSchedule = time to days
            showExactAlarmDialog = true
            return
        }

        // Check notification permission
        if (!PermissionHelper.hasNotificationPermission(context)) {
            pendingSchedule = time to days
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            return
        }

        // All permissions granted, add the schedule
        viewModel.addSchedule(time, days)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Schedule")
                        medicine?.let {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
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
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add schedule"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (schedules.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No schedules",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add a schedule to receive reminders",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(schedules, key = { it.id }) { schedule ->
                        ScheduleCard(
                            schedule = schedule,
                            onDelete = { viewModel.deleteSchedule(schedule) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddScheduleDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { time, days ->
                tryAddSchedule(time, days)
                showAddDialog = false
            }
        )
    }

    if (showExactAlarmDialog) {
        AlertDialog(
            onDismissRequest = {
                showExactAlarmDialog = false
                pendingSchedule = null
            },
            title = { Text("Permission Required") },
            text = {
                Text("To set medicine reminders, this app needs permission to schedule exact alarms. Please enable 'Alarms & reminders' for this app in Settings.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExactAlarmDialog = false
                        PermissionHelper.openExactAlarmSettings(context)
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExactAlarmDialog = false
                        pendingSchedule = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = {
                showNotificationDialog = false
                pendingSchedule = null
            },
            title = { Text("Notifications Disabled") },
            text = {
                Text("Without notification permission, you won't receive medicine reminders. You can enable notifications in Settings.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNotificationDialog = false
                        PermissionHelper.openNotificationSettings(context)
                        pendingSchedule = null
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNotificationDialog = false
                        // Still add the schedule even without notification permission
                        pendingSchedule?.let { (time, days) ->
                            viewModel.addSchedule(time, days)
                        }
                        pendingSchedule = null
                    }
                ) {
                    Text("Add Anyway")
                }
            }
        )
    }
}

@Composable
private fun ScheduleCard(
    schedule: MedicineSchedule,
    onDelete: () -> Unit
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.scheduledTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = getDayNames(schedule.daysOfWeek),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete schedule",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddScheduleDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalTime, Int) -> Unit
) {
    var selectedDays by remember { mutableIntStateOf(MedicineSchedule.ALL_DAYS) }
    val timePickerState = rememberTimePickerState(
        initialHour = 9,
        initialMinute = 0
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Schedule") },
        text = {
            Column {
                Text(
                    text = "Time",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                TimePicker(state = timePickerState)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Days",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                DayOfWeekSelector(
                    selectedDays = selectedDays,
                    onDaysChanged = { selectedDays = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    onConfirm(time, selectedDays)
                },
                enabled = selectedDays != 0
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
