package com.firexrwtinc.mytime.data.repository

import android.content.Context
import com.firexrwtinc.mytime.ui.settings.AppTheme
import com.firexrwtinc.mytime.ui.settings.Language
import com.firexrwtinc.mytime.ui.settings.NotificationSound
import kotlinx.coroutines.flow.Flow

/**
 * Singleton manager for application settings.
 * Provides global access to settings and handles immediate changes.
 * This class serves as the single source of truth for all application settings.
 */
class SettingsManager private constructor(context: Context) {
    
    private val repository = SettingsRepository(context.applicationContext)
    
    // Language callbacks
    private var onLanguageChangeCallback: ((Language) -> Unit)? = null
    
    // Theme callbacks  
    private var onThemeChangeCallback: ((AppTheme) -> Unit)? = null
    
    // Other settings change callbacks
    private var onSettingsChangeCallback: (() -> Unit)? = null
    
    // Exposed reactive streams from repository
    val language: Flow<Language> = repository.language
    val theme: Flow<AppTheme> = repository.theme
    val notificationsEnabled: Flow<Boolean> = repository.notificationsEnabled
    val notificationSound: Flow<NotificationSound> = repository.notificationSound
    val vibrationEnabled: Flow<Boolean> = repository.vibrationEnabled
    val defaultTaskDuration: Flow<Int> = repository.defaultTaskDuration
    val defaultReminderHours: Flow<Int> = repository.defaultReminderHours
    val showWeekends: Flow<Boolean> = repository.showWeekends
    
    // Register callbacks for immediate changes
    fun setLanguageChangeCallback(callback: (Language) -> Unit) {
        onLanguageChangeCallback = callback
    }
    
    fun setThemeChangeCallback(callback: (AppTheme) -> Unit) {
        onThemeChangeCallback = callback
    }
    
    fun setSettingsChangeCallback(callback: () -> Unit) {
        onSettingsChangeCallback = callback
    }
    
    // Language methods with immediate callback
    fun getLanguage(): Language = repository.getLanguage()
    
    fun setLanguage(language: Language) {
        repository.setLanguage(language)
        onLanguageChangeCallback?.invoke(language)
        onSettingsChangeCallback?.invoke()
    }
    
    // Theme methods with immediate callback
    fun getTheme(): AppTheme = repository.getTheme()
    
    fun setTheme(theme: AppTheme) {
        repository.setTheme(theme)
        onThemeChangeCallback?.invoke(theme)
        onSettingsChangeCallback?.invoke()
    }
    
    // Notification methods
    fun getNotificationsEnabled(): Boolean = repository.getNotificationsEnabled()
    
    fun setNotificationsEnabled(enabled: Boolean) {
        repository.setNotificationsEnabled(enabled)
        onSettingsChangeCallback?.invoke()
    }
    
    fun getNotificationSound(): NotificationSound = repository.getNotificationSound()
    
    fun setNotificationSound(sound: NotificationSound) {
        repository.setNotificationSound(sound)
        onSettingsChangeCallback?.invoke()
    }
    
    fun getVibrationEnabled(): Boolean = repository.getVibrationEnabled()
    
    fun setVibrationEnabled(enabled: Boolean) {
        repository.setVibrationEnabled(enabled)
        onSettingsChangeCallback?.invoke()
    }
    
    // Task default settings methods
    fun getDefaultTaskDuration(): Int = repository.getDefaultTaskDuration()
    
    fun setDefaultTaskDuration(durationMinutes: Int) {
        repository.setDefaultTaskDuration(durationMinutes)
        onSettingsChangeCallback?.invoke()
    }
    
    fun getDefaultReminderHours(): Int = repository.getDefaultReminderHours()
    
    fun setDefaultReminderHours(hours: Int) {
        repository.setDefaultReminderHours(hours)
        onSettingsChangeCallback?.invoke()
    }
    
    fun getShowWeekends(): Boolean = repository.getShowWeekends()
    
    fun setShowWeekends(show: Boolean) {
        repository.setShowWeekends(show)
        onSettingsChangeCallback?.invoke()
    }
    
    companion object {
        @Volatile
        private var INSTANCE: SettingsManager? = null
        
        /**
         * Gets the singleton instance of SettingsManager.
         * @param context Application context for initialization
         * @return SettingsManager instance
         */
        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsManager(context).also { INSTANCE = it }
            }
        }
        
        /**
         * Gets the singleton instance. Must be initialized first.
         * @throws IllegalStateException if not initialized
         */
        fun getInstance(): SettingsManager {
            return INSTANCE ?: throw IllegalStateException(
                "SettingsManager must be initialized with a context first"
            )
        }
    }
}