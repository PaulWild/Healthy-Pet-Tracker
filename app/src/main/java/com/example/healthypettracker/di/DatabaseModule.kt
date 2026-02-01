package com.example.healthypettracker.di

import android.content.Context
import com.example.healthypettracker.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = AppDatabase.getInstance(context)

    @Provides
    fun provideCatDao(db: AppDatabase) = db.catDao()
    @Provides
    fun provideMedicineDao(db: AppDatabase) = db.medicineDao()
    @Provides
    fun provideWeightDao(db: AppDatabase) = db.weightDao()
    @Provides
    fun provideFoodDao(db: AppDatabase) = db.foodDao()
    @Provides
    fun provideDiaryDao(db: AppDatabase) = db.diaryDao()
}
