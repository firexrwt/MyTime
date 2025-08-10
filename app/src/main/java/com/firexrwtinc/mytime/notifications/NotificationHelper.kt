package com.firexrwtinc.mytime.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.firexrwtinc.mytime.MainActivity
import com.firexrwtinc.mytime.R

class NotificationHelper(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID = "task_reminders"
        const val CHANNEL_NAME = "Напоминания о задачах"
        const val CHANNEL_DESCRIPTION = "Уведомления о приближающихся задачах"
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
                setBypassDnd(true) // Игнорировать режим "Не беспокоить"
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showTaskReminder(taskId: Long, taskTitle: String, taskTime: String, location: String? = null) {
        android.util.Log.e("NotificationHelper", "СОЗДАЕМ УВЕДОМЛЕНИЕ: $taskTitle (ID: $taskId)")
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("TASK_ID", taskId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            taskId.toInt(), 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Определяем тип уведомления по заголовку
        val isTaskStart = taskTitle.contains("НАЧАЛАСЬ!")
        val notificationTitle = if (isTaskStart) "🔔 ЗАДАЧА НАЧАЛАСЬ!" else "Напоминание о задаче"
        val priority = if (isTaskStart) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH
        
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(if (isTaskStart) android.R.drawable.ic_lock_idle_alarm else android.R.drawable.ic_dialog_info)
            .setContentTitle(notificationTitle)
            .setContentText("$taskTitle в $taskTime")
            .setPriority(priority)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Звук, вибрация, мигание
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(if (isTaskStart) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
        
        // Добавляем действие навигации, если есть координаты
        if (!location.isNullOrBlank()) {
            // Пытаемся парсить координаты из строки вида "55.7558, 37.6176"
            val coords = location.split(",").map { it.trim() }
            val navigationIntent = if (coords.size == 2) {
                try {
                    val lat = coords[0].toDouble()
                    val lng = coords[1].toDouble()
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("geo:$lat,$lng")
                    }
                } catch (e: NumberFormatException) {
                    // Если не координаты, используем как название места
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("geo:0,0?q=${Uri.encode(location)}")
                    }
                }
            } else {
                // Если не координаты, используем как название места
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("geo:0,0?q=${Uri.encode(location)}")
                }
            }
            
            val navigationPendingIntent = PendingIntent.getActivity(
                context,
                (taskId + 1000).toInt(), // Другой request code
                navigationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            notificationBuilder.addAction(
                android.R.drawable.ic_menu_mylocation, // Иконка локации
                "🧭 Навигация",
                navigationPendingIntent
            )
            
            // Обновляем текст уведомления, чтобы показать местоположение
            notificationBuilder.setContentText("$taskTitle в $taskTime • $location")
        }
        
        val notification = notificationBuilder.build()
        
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(taskId.toInt(), notification)
                android.util.Log.e("NotificationHelper", "✅ УВЕДОМЛЕНИЕ ПОКАЗАНО УСПЕШНО: $taskTitle")
            } else {
                android.util.Log.e("NotificationHelper", "❌ УВЕДОМЛЕНИЯ ОТКЛЮЧЕНЫ ПОЛЬЗОВАТЕЛЕМ")
            }
        } catch (e: SecurityException) {
            android.util.Log.e("NotificationHelper", "❌ НЕТ РАЗРЕШЕНИЯ НА УВЕДОМЛЕНИЯ", e)
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "❌ ОШИБКА ПОКАЗА УВЕДОМЛЕНИЯ", e)
        }
    }
}