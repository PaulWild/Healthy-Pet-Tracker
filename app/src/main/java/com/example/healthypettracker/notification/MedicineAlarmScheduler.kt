package com.example.healthypettracker.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.healthypettracker.data.local.entity.MedicineSchedule
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class MedicineAlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(
        schedule: MedicineSchedule,
        medicineName: String,
        catName: String,
        dosage: String?
    ) {
        val nextAlarmTime = calculateNextAlarmTime(schedule) ?: return

        val intent = Intent(context, MedicineReminderReceiver::class.java).apply {
            action = MedicineReminderReceiver.ACTION_REMINDER
            putExtra(MedicineReminderReceiver.EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(MedicineReminderReceiver.EXTRA_MEDICINE_ID, schedule.medicineId)
            putExtra(MedicineReminderReceiver.EXTRA_MEDICINE_NAME, medicineName)
            putExtra(MedicineReminderReceiver.EXTRA_CAT_NAME, catName)
            putExtra(MedicineReminderReceiver.EXTRA_DOSAGE, dosage)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = nextAlarmTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // SCHEDULE_EXACT_ALARM permission not granted
        }
    }

    fun scheduleSnooze(
        scheduleId: Long,
        medicineId: Long,
        medicineName: String,
        catName: String,
        dosage: String?,
        snoozeMinutes: Int = 15
    ) {
        val snoozeTime = LocalDateTime.now().plusMinutes(snoozeMinutes.toLong())

        val intent = Intent(context, MedicineReminderReceiver::class.java).apply {
            action = MedicineReminderReceiver.ACTION_REMINDER
            putExtra(MedicineReminderReceiver.EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(MedicineReminderReceiver.EXTRA_MEDICINE_ID, medicineId)
            putExtra(MedicineReminderReceiver.EXTRA_MEDICINE_NAME, medicineName)
            putExtra(MedicineReminderReceiver.EXTRA_CAT_NAME, catName)
            putExtra(MedicineReminderReceiver.EXTRA_DOSAGE, dosage)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (scheduleId.toInt() + 10000), // Different request code for snooze
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = snoozeTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // SCHEDULE_EXACT_ALARM permission not granted
        }
    }

    fun cancelAlarm(scheduleId: Long) {
        val intent = Intent(context, MedicineReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun calculateNextAlarmTime(schedule: MedicineSchedule): LocalDateTime? {
        val now = LocalDateTime.now()
        val today = LocalDate.now()
        val scheduledTime = schedule.scheduledTime

        // Check if schedule has ended
        if (schedule.endDate != null && today.isAfter(schedule.endDate)) {
            return null
        }

        // Check if schedule hasn't started yet
        val startDate = schedule.startDate ?: today
        if (today.isBefore(startDate)) {
            // Find first scheduled day on or after start date
            return findNextScheduledDay(startDate, scheduledTime, schedule.daysOfWeek)
        }

        // Check if today is a scheduled day and time hasn't passed
        val todayDateTime = LocalDateTime.of(today, scheduledTime)
        if (isScheduledOnDay(schedule.daysOfWeek, today.dayOfWeek) && todayDateTime.isAfter(now)) {
            return todayDateTime
        }

        // Find next scheduled day
        return findNextScheduledDay(today.plusDays(1), scheduledTime, schedule.daysOfWeek)
    }

    private fun findNextScheduledDay(
        fromDate: LocalDate,
        time: LocalTime,
        daysOfWeek: Int
    ): LocalDateTime? {
        var checkDate = fromDate
        repeat(7) {
            if (isScheduledOnDay(daysOfWeek, checkDate.dayOfWeek)) {
                return LocalDateTime.of(checkDate, time)
            }
            checkDate = checkDate.plusDays(1)
        }
        return null
    }

    private fun isScheduledOnDay(daysOfWeek: Int, dayOfWeek: DayOfWeek): Boolean {
        val dayBit = when (dayOfWeek) {
            DayOfWeek.SUNDAY -> MedicineSchedule.SUNDAY
            DayOfWeek.MONDAY -> MedicineSchedule.MONDAY
            DayOfWeek.TUESDAY -> MedicineSchedule.TUESDAY
            DayOfWeek.WEDNESDAY -> MedicineSchedule.WEDNESDAY
            DayOfWeek.THURSDAY -> MedicineSchedule.THURSDAY
            DayOfWeek.FRIDAY -> MedicineSchedule.FRIDAY
            DayOfWeek.SATURDAY -> MedicineSchedule.SATURDAY
        }
        return (daysOfWeek and dayBit) != 0
    }
}
