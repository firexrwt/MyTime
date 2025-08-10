package com.firexrwtinc.mytime.data.database

import androidx.room.*
import com.firexrwtinc.mytime.data.model.AppSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettings(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: AppSettings)

    @Update
    suspend fun updateSettings(settings: AppSettings)

    @Query("DELETE FROM app_settings")
    suspend fun deleteAllSettings()

    // Методы для быстрого обновления отдельных настроек
    @Query("UPDATE app_settings SET timeFormat24Hour = :timeFormat24Hour, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateTimeFormat(timeFormat24Hour: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE app_settings SET themeMode = :themeMode, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateThemeMode(themeMode: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE app_settings SET accentColor = :accentColor, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateAccentColor(accentColor: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE app_settings SET defaultNotificationMinutes = :minutes, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateDefaultNotificationMinutes(minutes: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE app_settings SET enableNotifications = :enabled, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateNotificationsEnabled(enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
}