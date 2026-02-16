package com.example.healthypettracker.data.repository

import com.example.healthypettracker.data.local.dao.MedicineDao
import com.example.healthypettracker.data.local.entity.Medicine
import com.example.healthypettracker.data.local.entity.MedicineLog
import com.example.healthypettracker.data.local.entity.MedicineSchedule
import com.example.healthypettracker.domain.repository.MedicineRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class MedicineRepositoryImpl(private val medicineDao: MedicineDao) : MedicineRepository {
    override fun getMedicinesForCat(catId: Long): Flow<List<Medicine>> =
        medicineDao.getMedicinesForCat(catId)

    override fun getActiveMedicinesForCat(catId: Long): Flow<List<Medicine>> =
        medicineDao.getActiveMedicinesForCat(catId)

    override suspend fun getMedicineById(medicineId: Long): Medicine? =
        medicineDao.getMedicineById(medicineId)

    override fun getMedicineByIdFlow(medicineId: Long): Flow<Medicine?> =
        medicineDao.getMedicineByIdFlow(medicineId)

    override suspend fun insertMedicine(medicine: Medicine): Long =
        medicineDao.insertMedicine(medicine)

    override suspend fun updateMedicine(medicine: Medicine) =
        medicineDao.updateMedicine(medicine)

    override suspend fun deleteMedicine(medicine: Medicine) =
        medicineDao.deleteMedicine(medicine)

    override fun getSchedulesForMedicine(medicineId: Long): Flow<List<MedicineSchedule>> =
        medicineDao.getSchedulesForMedicine(medicineId)

    override suspend fun getScheduleById(scheduleId: Long): MedicineSchedule? =
        medicineDao.getScheduleById(scheduleId)

    override suspend fun getAllActiveSchedules(): List<MedicineSchedule> =
        medicineDao.getAllActiveSchedules()

    override suspend fun insertSchedule(schedule: MedicineSchedule): Long =
        medicineDao.insertSchedule(schedule)

    override suspend fun updateSchedule(schedule: MedicineSchedule) =
        medicineDao.updateSchedule(schedule)

    override suspend fun deleteSchedule(schedule: MedicineSchedule) =
        medicineDao.deleteSchedule(schedule)

    override fun getLogsForMedicine(medicineId: Long): Flow<List<MedicineLog>> =
        medicineDao.getLogsForMedicine(medicineId)

    override fun getRecentLogsForMedicine(medicineId: Long, since: LocalDateTime): Flow<List<MedicineLog>> =
        medicineDao.getRecentLogsForMedicine(medicineId, since)

    override fun getLogsForMedicinesInRange(medicineIds: List<Long>, start: LocalDateTime, end: LocalDateTime): Flow<List<MedicineLog>> =
        medicineDao.getLogsForMedicinesInRange(medicineIds, start, end)

    override suspend fun insertLog(log: MedicineLog): Long =
        medicineDao.insertLog(log)

    override suspend fun deleteLog(log: MedicineLog) =
        medicineDao.deleteLog(log)
}
