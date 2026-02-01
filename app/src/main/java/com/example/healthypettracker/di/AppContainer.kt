package com.example.healthypettracker.di

import android.content.Context
import com.example.healthypettracker.data.local.AppDatabase
import com.example.healthypettracker.data.repository.CatRepositoryImpl
import com.example.healthypettracker.data.repository.DiaryRepositoryImpl
import com.example.healthypettracker.data.repository.FoodRepositoryImpl
import com.example.healthypettracker.data.repository.MedicineRepositoryImpl
import com.example.healthypettracker.data.repository.WeightRepositoryImpl
import com.example.healthypettracker.domain.repository.CatRepository
import com.example.healthypettracker.domain.repository.DiaryRepository
import com.example.healthypettracker.domain.repository.FoodRepository
import com.example.healthypettracker.domain.repository.MedicineRepository
import com.example.healthypettracker.domain.repository.WeightRepository
import com.example.healthypettracker.notification.MedicineAlarmScheduler

class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)

    val catRepository: CatRepository = CatRepositoryImpl(database.catDao())
    val medicineRepository: MedicineRepository = MedicineRepositoryImpl(database.medicineDao())
    val weightRepository: WeightRepository = WeightRepositoryImpl(database.weightDao())
    val foodRepository: FoodRepository = FoodRepositoryImpl(database.foodDao())
    val diaryRepository: DiaryRepository = DiaryRepositoryImpl(database.diaryDao())

    val medicineAlarmScheduler: MedicineAlarmScheduler = MedicineAlarmScheduler(context)
}
