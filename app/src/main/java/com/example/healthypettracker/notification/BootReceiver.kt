package com.example.healthypettracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.healthypettracker.domain.repository.CatRepository
import com.example.healthypettracker.domain.repository.MedicineRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var medicineRepository: MedicineRepository

    @Inject
    lateinit var catRepository: CatRepository

    @Inject
    lateinit var medicineAlarmScheduler: MedicineAlarmScheduler
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAllAlarms()
        }
    }

    private fun rescheduleAllAlarms() {

        CoroutineScope(Dispatchers.IO).launch {
            val activeSchedules = medicineRepository.getAllActiveSchedules()

            for (schedule in activeSchedules) {
                val medicine = medicineRepository.getMedicineById(schedule.medicineId)
                if (medicine != null && medicine.isActive) {
                    val cat = catRepository.getCatById(medicine.catId)
                    medicineAlarmScheduler.scheduleAlarm(
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
