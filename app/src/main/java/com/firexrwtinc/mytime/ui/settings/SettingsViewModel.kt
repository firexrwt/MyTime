package com.firexrwtinc.mytime.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.firexrwtinc.mytime.data.database.AppDatabase
import com.firexrwtinc.mytime.data.model.AppSettings
import com.firexrwtinc.mytime.data.model.ThemeMode
import com.firexrwtinc.mytime.notifications.NotificationScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val settingsDao = database.settingsDao()
    private val notificationScheduler = NotificationScheduler(application)
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Получаем настройки с дефолтными значениями
    val settings: StateFlow<AppSettings> = settingsDao.getSettingsFlow()
        .map { it ?: getDefaultSettings() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = getDefaultSettings()
        )
    
    private fun getDefaultSettings(): AppSettings {
        return AppSettings()
    }
    
    fun updateTimeFormat(use24Hour: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val currentSettings = settings.value
                val updatedSettings = currentSettings.copy(
                    timeFormat24Hour = use24Hour,
                    updatedAt = System.currentTimeMillis()
                )
                settingsDao.insertOrUpdateSettings(updatedSettings)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при обновлении формата времени: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val currentSettings = settings.value
                val updatedSettings = currentSettings.copy(
                    themeMode = themeMode,
                    updatedAt = System.currentTimeMillis()
                )
                settingsDao.insertOrUpdateSettings(updatedSettings)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при обновлении темы: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateAccentColor(colorHex: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val currentSettings = settings.value
                val updatedSettings = currentSettings.copy(
                    accentColor = colorHex,
                    updatedAt = System.currentTimeMillis()
                )
                settingsDao.insertOrUpdateSettings(updatedSettings)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при обновлении акцентного цвета: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateDefaultNotificationMinutes(minutes: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val currentSettings = settings.value
                val updatedSettings = currentSettings.copy(
                    defaultNotificationMinutes = minutes,
                    updatedAt = System.currentTimeMillis()
                )
                settingsDao.insertOrUpdateSettings(updatedSettings)
                // Перепланируем все уведомления с новым временем
                notificationScheduler.rescheduleAllTasks()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при обновлении времени уведомлений: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val currentSettings = settings.value
                val updatedSettings = currentSettings.copy(
                    enableNotifications = enabled,
                    updatedAt = System.currentTimeMillis()
                )
                settingsDao.insertOrUpdateSettings(updatedSettings)
                // Перепланируем все уведомления согласно новой настройке
                notificationScheduler.rescheduleAllTasks()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при обновлении настроек уведомлений: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateShowCompletedTasks(show: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val currentSettings = settings.value
                val updatedSettings = currentSettings.copy(
                    showCompletedTasks = show,
                    updatedAt = System.currentTimeMillis()
                )
                settingsDao.insertOrUpdateSettings(updatedSettings)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при обновлении отображения задач: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateAutoDeleteCompletedTasksAfterDays(days: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val currentSettings = settings.value
                val updatedSettings = currentSettings.copy(
                    autoDeleteCompletedTasksAfterDays = days,
                    updatedAt = System.currentTimeMillis()
                )
                settingsDao.insertOrUpdateSettings(updatedSettings)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при обновлении автоудаления: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    suspend fun initializeSettings() {
        viewModelScope.launch {
            try {
                val existingSettings = settingsDao.getSettings()
                if (existingSettings == null) {
                    // Создаем настройки по умолчанию
                    settingsDao.insertOrUpdateSettings(getDefaultSettings())
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при инициализации настроек: ${e.message}"
            }
        }
    }
}