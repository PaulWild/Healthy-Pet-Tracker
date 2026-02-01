package com.example.healthypettracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.healthypettracker.ui.navigation.AppNavigation
import com.example.healthypettracker.ui.theme.HealthyPetTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as HealthyPetTrackerApp

        setContent {
            HealthyPetTrackerTheme {
                AppNavigation(container = app.container)
            }
        }
    }
}
