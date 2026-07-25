package com.firexrwtinc.mytime

import android.app.Application
import com.firexrwtinc.mytime.data.repository.SettingsManager
import org.osmdroid.config.Configuration

/**
 * Custom Application class for MyTime app.
 * Initializes global components like SettingsManager and OSMDroid.
 */
class MyTimeApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize SettingsManager early in the application lifecycle
        SettingsManager.getInstance(this)

        // Initialize OSMDroid configuration
        Configuration.getInstance().apply {
            load(this@MyTimeApplication, getSharedPreferences("osmdroid", MODE_PRIVATE))
            userAgentValue = packageName
        }
    }
}