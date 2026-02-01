package com.example.healthypettracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.healthypettracker.data.local.entity.Medicine
import com.example.healthypettracker.data.local.entity.MedicineLog
import com.example.healthypettracker.data.local.entity.MedicineSchedule
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines WHERE catId = :catId ORDER BY name ASC")
    fun getMedicinesForCat(catId: Long): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE catId = :catId AND isActive = 1 ORDER BY name ASC")
    fun getActiveMedicinesForCat(catId: Long): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE id = :medicineId")
    suspend fun getMedicineById(medicineId: Long): Medicine?

    @Query("SELECT * FROM medicines WHERE id = :medicineId")
    fun getMedicineByIdFlow(medicineId: Long): Flow<Medicine?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: Medicine): Long

    @Update
    suspend fun updateMedicine(medicine: Medicine)

    @Delete
    suspend fun deleteMedicine(medicine: Medicine)

    // Schedules
    @Query("SELECT * FROM medicine_schedules WHERE medicineId = :medicineId")
    fun getSchedulesForMedicine(medicineId: Long): Flow<List<MedicineSchedule>>

    @Query("SELECT * FROM medicine_schedules WHERE id = :scheduleId")
    suspend fun getScheduleById(scheduleId: Long): MedicineSchedule?

    @Query("SELECT ms.* FROM medicine_schedules ms INNER JOIN medicines m ON ms.medicineId = m.id WHERE m.isActive = 1")
    suspend fun getAllActiveSchedules(): List<MedicineSchedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: MedicineSchedule): Long

    @Update
    suspend fun updateSchedule(schedule: MedicineSchedule)

    @Delete
    suspend fun deleteSchedule(schedule: MedicineSchedule)

    @Query("DELETE FROM medicine_schedules WHERE medicineId = :medicineId")
    suspend fun deleteSchedulesForMedicine(medicineId: Long)

    // Logs
    @Query("SELECT * FROM medicine_logs WHERE medicineId = :medicineId ORDER BY administeredAt DESC")
    fun getLogsForMedicine(medicineId: Long): Flow<List<MedicineLog>>

    @Query("SELECT * FROM medicine_logs WHERE medicineId = :medicineId AND administeredAt >= :since ORDER BY administeredAt DESC")
    fun getRecentLogsForMedicine(medicineId: Long, since: LocalDateTime): Flow<List<MedicineLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MedicineLog): Long

    @Delete
    suspend fun deleteLog(log: MedicineLog)
}
