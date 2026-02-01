package com.example.healthypettracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.healthypettracker.HealthyPetTrackerApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAllAlarms(context)
        }
    }

    private fun rescheduleAllAlarms(context: Context) {
        val app = context.applicationContext as HealthyPetTrackerApp
        val medicineRepository = app.container.medicineRepository
        val alarmScheduler = app.container.medicineAlarmScheduler

        CoroutineScope(Dispatchers.IO).launch {
            val activeSchedules = medicineRepository.getAllActiveSchedules()

            for (schedule in activeSchedules) {
                val medicine = medicineRepository.getMedicineById(schedule.medicineId)
                if (medicine != null && medicine.isActive) {
                    val cat = app.container.catRepository.getCatById(medicine.catId)
                    alarmScheduler.scheduleAlarm(
                        schedule,
                        medicine.name,
                        cat?.name ?: "your cat",
                        medicine.dosage
                    )
                }
            }
        }
    }
}
