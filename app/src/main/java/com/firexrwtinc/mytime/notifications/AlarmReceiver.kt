package com.firexrwtinc.mytime.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.e("AlarmReceiver", "ПОЛУЧИЛИ АЛАРМ!!! Action: ${intent.action}")
        Log.e("AlarmReceiver", "Intent extras: ${intent.extras}")
        
        try {
            val taskId = intent.getLongExtra("TASK_ID", -1L)
            val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Задача"
            val taskTime = intent.getStringExtra("TASK_TIME") ?: ""
            val taskLocation = intent.getStringExtra("TASK_LOCATION")
            val isTaskStart = intent.getBooleanExtra("IS_TASK_START", false)
            
            Log.e("AlarmReceiver", "ДЕТАЛИ ЗАДАЧИ - ID: $taskId, Title: $taskTitle, Time: $taskTime, Location: $taskLocation, IsStart: $isTaskStart")
            
            if (taskId != -1L) {
                if (isTaskStart) {
                    Log.e("AlarmReceiver", "ПОКАЗЫВАЕМ БУДИЛЬНИК НА ВЕСЬ ЭКРАН!")
                    // Запускаем AlarmActivity для будильника
                    val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                        putExtra("TASK_ID", taskId)
                        putExtra("TASK_TITLE", taskTitle)
                        putExtra("TASK_TIME", taskTime)
                        putExtra("TASK_LOCATION", taskLocation)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    context.startActivity(alarmIntent)
                    Log.e("AlarmReceiver", "БУДИЛЬНИК ЗАПУЩЕН!")
                } else {
                    Log.e("AlarmReceiver", "ПОКАЗЫВАЕМ ОБЫЧНОЕ УВЕДОМЛЕНИЕ!")
                    val notificationHelper = NotificationHelper(context)
                    notificationHelper.showTaskReminder(taskId, taskTitle, taskTime, taskLocation)
                    Log.e("AlarmReceiver", "УВЕДОМЛЕНИЕ ОТПРАВЛЕНО!")
                }
            } else {
                Log.e("AlarmReceiver", "НЕВЕРНЫЙ ID ЗАДАЧИ: $taskId")
            }
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "ОШИБКА В АЛАРМРЕCEIVER", e)
        }
    }
}