package com.firexrwtinc.mytime.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.firexrwtinc.mytime.ui.settings.AppTheme
import com.firexrwtinc.mytime.ui.settings.Language
import com.firexrwtinc.mytime.ui.settings.NotificationSound
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing application settings using SharedPreferences.
 * Provides reactive access to settings through Flow and maintains type safety.
 */
class SettingsRepository(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        SETTINGS_PREFERENCES_NAME, 
        Context.MODE_PRIVATE
    )
    
    // Language setting
    private val _language = MutableStateFlow(getLanguage())
    val language: Flow<Language> = _language.asStateFlow()
    
    // Notification settings
    private val _notificationsEnabled = MutableStateFlow(getNotificationsEnabled())
    val notificationsEnabled: Flow<Boolean> = _notificationsEnabled.asStateFlow()
    
    private val _notificationSound = MutableStateFlow(getNotificationSound())
    val notificationSound: Flow<NotificationSound> = _notificationSound.asStateFlow()
    
    private val _vibrationEnabled = MutableStateFlow(getVibrationEnabled())
    val vibrationEnabled: Flow<Boolean> = _vibrationEnabled.asStateFlow()
    
    // Theme setting
    private val _theme = MutableStateFlow(getTheme())
    val theme: Flow<AppTheme> = _theme.asStateFlow()
    
    // Task default settings
    private val _defaultTaskDuration = MutableStateFlow(getDefaultTaskDuration())
    val defaultTaskDuration: Flow<Int> = _defaultTaskDuration.asStateFlow()
    
    private val _defaultReminderHours = MutableStateFlow(getDefaultReminderHours())
    val defaultReminderHours: Flow<Int> = _defaultReminderHours.asStateFlow()
    
    private val _showWeekends = MutableStateFlow(getShowWeekends())
    val showWeekends: Flow<Boolean> = _showWeekends.asStateFlow()
    
    // Language methods
    fun getLanguage(): Language {
        val languageName = sharedPreferences.getString(KEY_LANGUAGE, Language.SYSTEM.name) ?: Language.SYSTEM.name
        return try {
            Language.valueOf(languageName)
        } catch (e: IllegalArgumentException) {
            Language.SYSTEM
        }
    }
    
    fun setLanguage(language: Language) {
        sharedPreferences.edit()
            .putString(KEY_LANGUAGE, language.name)
            .apply()
        _language.value = language
    }
    
    // Notification methods
    fun getNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
            .apply()
        _notificationsEnabled.value = enabled
    }
    
    fun getNotificationSound(): NotificationSound {
        val soundName = sharedPreferences.getString(KEY_NOTIFICATION_SOUND, NotificationSound.DEFAULT.name) 
            ?: NotificationSound.DEFAULT.name
        return try {
            NotificationSound.valueOf(soundName)
        } catch (e: IllegalArgumentException) {
            NotificationSound.DEFAULT
        }
    }
    
    fun setNotificationSound(sound: NotificationSound) {
        sharedPreferences.edit()
            .putString(KEY_NOTIFICATION_SOUND, sound.name)
            .apply()
        _notificationSound.value = sound
    }
    
    fun getVibrationEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_VIBRATION_ENABLED, true)
    }
    
    fun setVibrationEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_VIBRATION_ENABLED, enabled)
            .apply()
        _vibrationEnabled.value = enabled
    }
    
    // Theme methods
    fun getTheme(): AppTheme {
        val themeName = sharedPreferences.getString(KEY_THEME, AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        return try {
            AppTheme.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            AppTheme.SYSTEM
        }
    }
    
    fun setTheme(theme: AppTheme) {
        sharedPreferences.edit()
            .putString(KEY_THEME, theme.name)
            .apply()
        _theme.value = theme
    }
    
    // Task default settings methods
    fun getDefaultTaskDuration(): Int {
        return sharedPreferences.getInt(KEY_DEFAULT_TASK_DURATION, 60)
    }
    
    fun setDefaultTaskDuration(durationMinutes: Int) {
        sharedPreferences.edit()
            .putInt(KEY_DEFAULT_TASK_DURATION, durationMinutes)
            .apply()
        _defaultTaskDuration.value = durationMinutes
    }
    
    fun getDefaultReminderHours(): Int {
        return sharedPreferences.getInt(KEY_DEFAULT_REMINDER_HOURS, 1)
    }
    
    fun setDefaultReminderHours(hours: Int) {
        sharedPreferences.edit()
            .putInt(KEY_DEFAULT_REMINDER_HOURS, hours)
            .apply()
        _defaultReminderHours.value = hours
    }
    
    fun getShowWeekends(): Boolean {
        return sharedPreferences.getBoolean(KEY_SHOW_WEEKENDS, true)
    }
    
    fun setShowWeekends(show: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_SHOW_WEEKENDS, show)
            .apply()
        _showWeekends.value = show
    }
    
    companion object {
        private const val SETTINGS_PREFERENCES_NAME = "mytime_settings"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_NOTIFICATION_SOUND = "notification_sound"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_THEME = "theme"
        private const val KEY_DEFAULT_TASK_DURATION = "default_task_duration"
        private const val KEY_DEFAULT_REMINDER_HOURS = "default_reminder_hours"
        private const val KEY_SHOW_WEEKENDS = "show_weekends"
    }
}