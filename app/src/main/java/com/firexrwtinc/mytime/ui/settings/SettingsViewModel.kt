package com.firexrwtinc.mytime.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.firexrwtinc.mytime.data.repository.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val selectedLanguage: Language = Language.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val notificationSound: NotificationSound = NotificationSound.DEFAULT,
    val vibrationEnabled: Boolean = true,
    val selectedTheme: AppTheme = AppTheme.SYSTEM,
    val defaultTaskDurationMinutes: Int = 60,
    val defaultReminderHours: Int = 1,
    val showWeekends: Boolean = true
)

enum class Language {
    SYSTEM, ENGLISH, RUSSIAN
}

enum class NotificationSound {
    DEFAULT, BELL, CHIME, NONE
}

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val settingsManager = SettingsManager.getInstance(application)
    
    // Combine all settings flows into a single UI state
    val uiState: StateFlow<SettingsUiState> = combine(
        settingsManager.language,
        settingsManager.notificationsEnabled,
        settingsManager.notificationSound,
        settingsManager.vibrationEnabled,
        settingsManager.theme,
        settingsManager.defaultTaskDuration,
        settingsManager.defaultReminderHours,
        settingsManager.showWeekends
    ) { flows ->
        val language = flows[0] as Language
        val notificationsEnabled = flows[1] as Boolean
        val notificationSound = flows[2] as NotificationSound
        val vibrationEnabled = flows[3] as Boolean
        val theme = flows[4] as AppTheme
        val defaultTaskDuration = flows[5] as Int
        val defaultReminderHours = flows[6] as Int
        val showWeekends = flows[7] as Boolean
        
        SettingsUiState(
            selectedLanguage = language,
            notificationsEnabled = notificationsEnabled,
            notificationSound = notificationSound,
            vibrationEnabled = vibrationEnabled,
            selectedTheme = theme,
            defaultTaskDurationMinutes = defaultTaskDuration,
            defaultReminderHours = defaultReminderHours,
            showWeekends = showWeekends
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(
            selectedLanguage = settingsManager.getLanguage(),
            notificationsEnabled = settingsManager.getNotificationsEnabled(),
            notificationSound = settingsManager.getNotificationSound(),
            vibrationEnabled = settingsManager.getVibrationEnabled(),
            selectedTheme = settingsManager.getTheme(),
            defaultTaskDurationMinutes = settingsManager.getDefaultTaskDuration(),
            defaultReminderHours = settingsManager.getDefaultReminderHours(),
            showWeekends = settingsManager.getShowWeekends()
        )
    )
    
    // Settings update methods - these will trigger immediate changes through SettingsManager
    fun updateLanguage(language: Language) {
        settingsManager.setLanguage(language)
    }
    
    fun updateNotificationsEnabled(enabled: Boolean) {
        settingsManager.setNotificationsEnabled(enabled)
    }
    
    fun updateNotificationSound(sound: NotificationSound) {
        settingsManager.setNotificationSound(sound)
    }
    
    fun updateVibrationEnabled(enabled: Boolean) {
        settingsManager.setVibrationEnabled(enabled)
    }
    
    fun updateTheme(theme: AppTheme) {
        settingsManager.setTheme(theme)
    }
    
    fun updateDefaultTaskDuration(durationMinutes: Int) {
        settingsManager.setDefaultTaskDuration(durationMinutes)
    }
    
    fun updateDefaultReminderHours(hours: Int) {
        settingsManager.setDefaultReminderHours(hours)
    }
    
    fun updateShowWeekends(show: Boolean) {
        settingsManager.setShowWeekends(show)
    }
}