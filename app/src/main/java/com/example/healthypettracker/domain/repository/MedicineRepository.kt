package com.example.healthypettracker.domain.repository

import com.example.healthypettracker.data.local.entity.Medicine
import com.example.healthypettracker.data.local.entity.MedicineLog
import com.example.healthypettracker.data.local.entity.MedicineSchedule
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface MedicineRepository {
    fun getMedicinesForCat(catId: Long): Flow<List<Medicine>>
    fun getActiveMedicinesForCat(catId: Long): Flow<List<Medicine>>
    suspend fun getMedicineById(medicineId: Long): Medicine?
    fun getMedicineByIdFlow(medicineId: Long): Flow<Medicine?>
    suspend fun insertMedicine(medicine: Medicine): Long
    suspend fun updateMedicine(medicine: Medicine)
    suspend fun deleteMedicine(medicine: Medicine)

    fun getSchedulesForMedicine(medicineId: Long): Flow<List<MedicineSchedule>>
    suspend fun getScheduleById(scheduleId: Long): MedicineSchedule?
    suspend fun getAllActiveSchedules(): List<MedicineSchedule>
    suspend fun insertSchedule(schedule: MedicineSchedule): Long
    suspend fun updateSchedule(schedule: MedicineSchedule)
    suspend fun deleteSchedule(schedule: MedicineSchedule)

    fun getLogsForMedicine(medicineId: Long): Flow<List<MedicineLog>>
    fun getRecentLogsForMedicine(medicineId: Long, since: LocalDateTime): Flow<List<MedicineLog>>
    suspend fun insertLog(log: MedicineLog): Long
    suspend fun deleteLog(log: MedicineLog)
}
