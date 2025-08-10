package com.firexrwtinc.mytime.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.firexrwtinc.mytime.data.model.Task
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class AlarmManagerHelper(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    fun scheduleTaskReminder(task: Task, reminderMinutes: Int? = null) {
        val taskDateTime = LocalDateTime.of(task.date, task.startTime)
        
        // Планируем уведомление за указанное время ДО задачи
        val minutes = reminderMinutes ?: (task.reminderHoursBefore?.times(60)) ?: 30
        val reminderDateTime = taskDateTime.minusMinutes(minutes.toLong())
        
        Log.e("AlarmManager", "ПЛАНИРУЕМ АЛАРМЫ ДЛЯ ЗАДАЧИ: ${task.title}")
        Log.e("AlarmManager", "Время задачи: $taskDateTime, Время напоминания: $reminderDateTime")
        
        // Планируем напоминание только если время не в прошлом
        if (!reminderDateTime.isBefore(LocalDateTime.now())) {
            scheduleAlarm(task, reminderDateTime, "напоминание")
        }
        
        // ПЛАНИРУЕМ БУДИЛЬНИК ТОЧНО НА ВРЕМЯ НАЧАЛА ЗАДАЧИ
        if (!taskDateTime.isBefore(LocalDateTime.now())) {
            scheduleAlarm(task, taskDateTime, "начало задачи", isTaskStart = true)
        } else {
            Log.d("AlarmManager", "Task start time is in the past, skipping task start alarm")
        }
    }
    
    private fun scheduleAlarm(task: Task, triggerDateTime: LocalDateTime, description: String, isTaskStart: Boolean = false) {
        val now = LocalDateTime.now()
        
        // ТЕСТИРОВАНИЕ: всегда ставим аларм через 10 секунд от текущего времени
        val testDateTime = now.plusSeconds(10)
        
        Log.e("AlarmManager", "ПЛАНИРУЕМ $description для задачи: ${task.title}")
        Log.e("AlarmManager", "Оригинальное время: $triggerDateTime")
        Log.e("AlarmManager", "ТЕСТОВОЕ ВРЕМЯ: $testDateTime")
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("TASK_TITLE", if (isTaskStart) "🚨 ${task.title} - ВРЕМЯ НАЧИНАТЬ!" else "📢 ${task.title}")
            putExtra("TASK_TIME", task.startTime.format(DateTimeFormatter.ofPattern("HH:mm")))
            putExtra("TASK_LOCATION", task.location)
            putExtra("IS_TASK_START", isTaskStart)
            action = "com.firexrwtinc.mytime.ALARM_ACTION"
        }
        
        val requestCode = if (isTaskStart) (task.id + 20000).toInt() else task.id.toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val zonedDateTime = testDateTime.atZone(ZoneId.systemDefault())
        val triggerTime = zonedDateTime.toInstant().toEpochMilli()
        
        Log.e("AlarmManager", "Аларм будет в: $testDateTime (через ${java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(triggerTime - System.currentTimeMillis())} секунд)")
        
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.e("AlarmManager", "✅ EXACT АЛАРМ УСТАНОВЛЕН для задачи: ${task.title} ($description)")
        } catch (e: SecurityException) {
            Log.e("AlarmManager", "❌ НЕТ РАЗРЕШЕНИЯ НА EXACT АЛАРМ", e)
            try {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.e("AlarmManager", "✅ ОБЫЧНЫЙ АЛАРМ УСТАНОВЛЕН для задачи: ${task.title} ($description)")
            } catch (e2: Exception) {
                Log.e("AlarmManager", "❌❌ ВСЕ АЛАРМЫ ПРОВАЛИЛИСЬ", e2)
            }
        }
    }
    
    fun cancelTaskReminder(taskId: Long) {
        // Отменяем уведомление напоминания
        val reminderIntent = Intent(context, AlarmReceiver::class.java)
        val reminderPendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            reminderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(reminderPendingIntent)
        
        // Отменяем будильник начала задачи
        val startIntent = Intent(context, AlarmReceiver::class.java)
        val startPendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId + 20000).toInt(),
            startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(startPendingIntent)
        
        Log.d("AlarmManager", "All alarms cancelled for task ID: $taskId")
    }
}