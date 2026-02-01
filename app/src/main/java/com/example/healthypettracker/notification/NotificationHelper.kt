package com.example.healthypettracker.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.healthypettracker.HealthyPetTrackerApp
import com.example.healthypettracker.MainActivity
import com.example.healthypettracker.R

object NotificationHelper {
    private const val REQUEST_CODE_OPEN_APP = 1000
    private const val REQUEST_CODE_MARK_GIVEN = 2000
    private const val REQUEST_CODE_SNOOZE = 3000

    fun showMedicineReminder(
        context: Context,
        scheduleId: Long,
        medicineId: Long,
        medicineName: String,
        catName: String,
        dosage: String?
    ) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("medicineId", medicineId)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_APP + scheduleId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val markGivenIntent = Intent(context, MedicineReminderReceiver::class.java).apply {
            action = MedicineReminderReceiver.ACTION_MARK_GIVEN
            putExtra(MedicineReminderReceiver.EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(MedicineReminderReceiver.EXTRA_MEDICINE_ID, medicineId)
        }
        val markGivenPendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_MARK_GIVEN + scheduleId.toInt(),
            markGivenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, MedicineReminderReceiver::class.java).apply {
            action = MedicineReminderReceiver.ACTION_SNOOZE
            putExtra(MedicineReminderReceiver.EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(MedicineReminderReceiver.EXTRA_MEDICINE_ID, medicineId)
            putExtra(MedicineReminderReceiver.EXTRA_MEDICINE_NAME, medicineName)
            putExtra(MedicineReminderReceiver.EXTRA_CAT_NAME, catName)
            putExtra(MedicineReminderReceiver.EXTRA_DOSAGE, dosage)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_SNOOZE + scheduleId.toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = buildString {
            append("Time to give $medicineName to $catName")
            if (!dosage.isNullOrBlank()) {
                append(" - $dosage")
            }
        }

        val notification = NotificationCompat.Builder(context, HealthyPetTrackerApp.MEDICINE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Medicine Reminder")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .addAction(0, "Mark Given", markGivenPendingIntent)
            .addAction(0, "Snooze 15min", snoozePendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(scheduleId.toInt(), notification)
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }

    fun cancelNotification(context: Context, scheduleId: Long) {
        NotificationManagerCompat.from(context).cancel(scheduleId.toInt())
    }
}
