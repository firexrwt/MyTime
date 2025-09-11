package com.firexrwtinc.mytime

import android.app.Application
import com.firexrwtinc.mytime.data.repository.SettingsManager

/**
 * Custom Application class for MyTime app.
 * Initializes global components like SettingsManager.
 */
class MyTimeApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize SettingsManager early in the application lifecycle
        SettingsManager.getInstance(this)
    }
}