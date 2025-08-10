package com.firexrwtinc.mytime.notifications

import android.content.Context
import com.firexrwtinc.mytime.data.database.AppDatabase
import com.firexrwtinc.mytime.data.model.Task
import kotlinx.coroutines.flow.first

class NotificationScheduler(private val context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val settingsDao = database.settingsDao()
    private val alarmManagerHelper = AlarmManagerHelper(context)
    
    fun scheduleNotificationForTask(task: Task) {
        android.util.Log.d("NotificationScheduler", "Scheduling notification for task: ${task.title} (ID: ${task.id})")
        
        try {
            // ВСЕГДА планируем уведомление с дефолтом 30 минут
            val reminderMinutes = when {
                task.reminderHoursBefore != null && task.reminderHoursBefore!! > 0 -> {
                    task.reminderHoursBefore!! * 60
                }
                else -> 30 // По умолчанию 30 минут
            }
            
            android.util.Log.d("NotificationScheduler", "Reminder time: $reminderMinutes minutes before")
            alarmManagerHelper.scheduleTaskReminder(task, reminderMinutes)
            android.util.Log.d("NotificationScheduler", "Notification scheduled successfully")
            
        } catch (e: Exception) {
            android.util.Log.e("NotificationScheduler", "Error scheduling notification for task ${task.id}", e)
        }
    }
    
    suspend fun rescheduleAllTasks() {
        try {
            val taskDao = database.taskDao()
            val tasks = taskDao.getAllTasks().first()
            
            // Отменяем все существующие уведомления
            tasks.forEach { task ->
                alarmManagerHelper.cancelTaskReminder(task.id)
            }
            
            // Планируем уведомления заново с новыми настройками
            tasks.forEach { task ->
                if (!task.isCompleted) {
                    scheduleNotificationForTask(task)
                }
            }
            
        } catch (e: Exception) {
            android.util.Log.e("NotificationScheduler", "Error rescheduling all tasks", e)
        }
    }
    
    fun cancelNotificationForTask(taskId: Long) {
        alarmManagerHelper.cancelTaskReminder(taskId)
    }
}