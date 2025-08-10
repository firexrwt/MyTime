package com.firexrwtinc.mytime.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1, // Всегда будет одна запись с id=1
    
    // Настройки времени
    val timeFormat24Hour: Boolean = true, // true для 24-часового формата, false для 12-часового
    
    // Настройки темы
    val themeMode: ThemeMode = ThemeMode.SYSTEM, // Светлая, темная или системная тема
    val accentColor: String = "#FF6200EA", // Акцентный цвет в формате hex
    
    // Настройки уведомлений
    val defaultNotificationMinutes: Int = 60, // Уведомления по умолчанию за 60 минут
    val enableNotifications: Boolean = true,
    
    // Языковые настройки
    val language: String = "ru", // Язык интерфейса
    
    // Другие настройки
    val showCompletedTasks: Boolean = true,
    val autoDeleteCompletedTasksAfterDays: Int = 30, // 0 = не удалять автоматически
    
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}