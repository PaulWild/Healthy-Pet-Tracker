package com.example.healthypettracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.healthypettracker.HealthyPetTrackerApp
import com.example.healthypettracker.data.local.entity.MedicineLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MedicineReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1)
        val medicineId = intent.getLongExtra(EXTRA_MEDICINE_ID, -1)
        val medicineName = intent.getStringExtra(EXTRA_MEDICINE_NAME) ?: "Medicine"
        val catName = intent.getStringExtra(EXTRA_CAT_NAME) ?: "your cat"
        val dosage = intent.getStringExtra(EXTRA_DOSAGE)

        when (intent.action) {
            ACTION_REMINDER -> {
                // Show notification
                NotificationHelper.showMedicineReminder(
                    context,
                    scheduleId,
                    medicineId,
                    medicineName,
                    catName,
                    dosage
                )

                // Reschedule for next occurrence
                rescheduleNextAlarm(context, scheduleId, medicineName, catName, dosage)
            }

            ACTION_MARK_GIVEN -> {
                // Log the medicine as given
                logMedicineGiven(context, medicineId)

                // Cancel the notification
                NotificationHelper.cancelNotification(context, scheduleId)
            }

            ACTION_SNOOZE -> {
                // Cancel current notification
                NotificationHelper.cancelNotification(context, scheduleId)

                // Schedule snooze alarm
                val app = context.applicationContext as HealthyPetTrackerApp
                app.container.medicineAlarmScheduler.scheduleSnooze(
                    scheduleId,
                    medicineId,
                    medicineName,
                    catName,
                    dosage
                )
            }
        }
    }

    private fun rescheduleNextAlarm(
        context: Context,
        scheduleId: Long,
        medicineName: String,
        catName: String,
        dosage: String?
    ) {
        val app = context.applicationContext as HealthyPetTrackerApp
        CoroutineScope(Dispatchers.IO).launch {
            val schedule = app.container.medicineRepository.getScheduleById(scheduleId)
            if (schedule != null) {
                app.container.medicineAlarmScheduler.scheduleAlarm(
                    schedule,
                    medicineName,
                    catName,
                    dosage
                )
            }
        }
    }

    private fun logMedicineGiven(context: Context, medicineId: Long) {
        val app = context.applicationContext as HealthyPetTrackerApp
        CoroutineScope(Dispatchers.IO).launch {
            val log = MedicineLog(
                medicineId = medicineId,
                administeredAt = LocalDateTime.now(),
                wasSkipped = false
            )
            app.container.medicineRepository.insertLog(log)
        }
    }

    companion object {
        const val ACTION_REMINDER = "com.example.healthypettracker.ACTION_REMINDER"
        const val ACTION_MARK_GIVEN = "com.example.healthypettracker.ACTION_MARK_GIVEN"
        const val ACTION_SNOOZE = "com.example.healthypettracker.ACTION_SNOOZE"

        const val EXTRA_SCHEDULE_ID = "schedule_id"
        const val EXTRA_MEDICINE_ID = "medicine_id"
        const val EXTRA_MEDICINE_NAME = "medicine_name"
        const val EXTRA_CAT_NAME = "cat_name"
        const val EXTRA_DOSAGE = "dosage"
    }
}
