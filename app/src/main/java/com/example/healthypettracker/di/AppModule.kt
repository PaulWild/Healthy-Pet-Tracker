package com.example.healthypettracker.di

import android.content.Context
import com.example.healthypettracker.notification.MedicineAlarmScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMedicineAlarmScheduler(
        @ApplicationContext context: Context
    ): MedicineAlarmScheduler = MedicineAlarmScheduler(context)
}
