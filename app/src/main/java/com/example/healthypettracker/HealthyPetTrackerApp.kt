package com.example.healthypettracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HealthyPetTrackerApp : Application() {


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            MEDICINE_CHANNEL_ID,
            "Medicine Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for medicine reminders"
            enableVibration(true)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val MEDICINE_CHANNEL_ID = "medicine_reminders"
    }
}
