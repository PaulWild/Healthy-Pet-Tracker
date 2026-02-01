package com.example.healthypettracker

import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.healthypettracker.ui.navigation.AppNavigation
import com.example.healthypettracker.ui.theme.HealthyPetTrackerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as HealthyPetTrackerApp

        setContent {
            var showAlarmPermissionDialog by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                    if (!alarmManager.canScheduleExactAlarms()) {
                        showAlarmPermissionDialog = true
                    }
                }
            }

            HealthyPetTrackerTheme {
                AppNavigation(container = app.container)

                if (showAlarmPermissionDialog) {
                    AlarmPermissionDialog(
                        onDismiss = { showAlarmPermissionDialog = false },
                        onOpenSettings = {
                            showAlarmPermissionDialog = false
                            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmPermissionDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enable Reminders") },
        text = { Text("To receive medicine reminders at the exact scheduled time, please allow this app to schedule alarms.") },
        confirmButton = {
            TextButton(onClick = onOpenSettings) { Text("Open Settings") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Later") }
        }
    )
}
