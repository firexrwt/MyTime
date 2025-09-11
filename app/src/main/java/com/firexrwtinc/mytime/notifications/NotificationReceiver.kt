package com.firexrwtinc.mytime.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.firexrwtinc.mytime.MainActivity
import com.firexrwtinc.mytime.R

/**
 * BroadcastReceiver that handles alarm events and displays task reminder notifications.
 * This receiver is triggered by AlarmManager when a scheduled task reminder is due.
 */
class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "task_reminders"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TASK_TITLE = "task_title"
        const val EXTRA_TASK_LOCATION = "task_location"
        const val EXTRA_TASK_START_TIME = "task_start_time"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: return
        val taskLocation = intent.getStringExtra(EXTRA_TASK_LOCATION)
        val taskStartTime = intent.getStringExtra(EXTRA_TASK_START_TIME) ?: ""

        // Create notification channel if needed (Android 8.0+)
        createNotificationChannel(context)

        // Show the notification
        showNotification(context, taskId, taskTitle, taskLocation, taskStartTime)
    }

    /**
     * Creates a notification channel for task reminders on Android 8.0+ devices.
     * This is required for notifications to work on newer Android versions.
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Displays a notification for the task reminder.
     * The notification includes task title, start time, and location (if available).
     */
    private fun showNotification(
        context: Context,
        taskId: Long,
        taskTitle: String,
        taskLocation: String?,
        taskStartTime: String
    ) {
        // Create intent to open the app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("task_id", taskId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification content
        val contentText = buildNotificationContent(context, taskStartTime, taskLocation)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle(taskTitle)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Show the notification
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(taskId.toInt(), notification)
            }
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
            // In production, you might want to log this or handle gracefully
        }
    }

    /**
     * Builds the notification content text including start time and location.
     */
    private fun buildNotificationContent(
        context: Context,
        taskStartTime: String,
        taskLocation: String?
    ): String {
        val timeText = context.getString(R.string.notification_start_time, taskStartTime)
        return if (taskLocation.isNullOrBlank()) {
            timeText
        } else {
            "$timeText\n${context.getString(R.string.notification_location, taskLocation)}"
        }
    }
}