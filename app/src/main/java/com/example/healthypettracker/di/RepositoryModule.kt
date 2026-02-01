package com.example.healthypettracker.di

import com.example.healthypettracker.data.local.dao.CatDao
import com.example.healthypettracker.data.local.dao.DiaryDao
import com.example.healthypettracker.data.local.dao.FoodDao
import com.example.healthypettracker.data.local.dao.MedicineDao
import com.example.healthypettracker.data.local.dao.WeightDao
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideCatRepository(dao: CatDao): CatRepository =
        CatRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideMedicineRepository(dao: MedicineDao): MedicineRepository =
        MedicineRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideWeightRepository(dao: WeightDao): WeightRepository =
        WeightRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideFoodRepository(dao: FoodDao): FoodRepository =
        FoodRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideDiaryRepository(dao: DiaryDao): DiaryRepository =
        DiaryRepositoryImpl(dao)
}
