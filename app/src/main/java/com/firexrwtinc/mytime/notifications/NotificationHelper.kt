package com.firexrwtinc.mytime.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.firexrwtinc.mytime.data.model.Task
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.time.format.DateTimeFormatter

/**
 * Helper class for managing task reminder notifications using AlarmManager.
 * Provides functionality to schedule, update, and cancel alarms for task reminders.
 */
class NotificationHelper(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    companion object {
        private const val TAG = "NotificationHelper"
    }

    /**
     * Schedules a notification alarm for the given task.
     * The alarm will trigger at the calculated reminder time (task start time - reminderHoursBefore).
     */
    fun scheduleTaskReminder(task: Task) {
        // Only schedule if reminder is set and task hasn't passed
        val reminderHours = task.reminderHoursBefore ?: return
        if (reminderHours <= 0) return

        val taskDateTime = LocalDateTime.of(task.date, task.startTime)
        val reminderDateTime = taskDateTime.minusHours(reminderHours.toLong())
        
        // Don't schedule if reminder time has already passed
        if (reminderDateTime.isBefore(LocalDateTime.now())) {
            Log.d(TAG, "Reminder time has passed for task: ${task.title}")
            return
        }

        val intent = createNotificationIntent(task)
        val pendingIntent = createPendingIntent(task.id, intent)

        val triggerTime = reminderDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    // Use setExactAndAllowWhileIdle for reliable delivery on Android 6.0+
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                    // Use setExact for Android 4.4+
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
                else -> {
                    // Fallback for older versions
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            }

            Log.d(TAG, "Scheduled reminder for task '${task.title}' at $reminderDateTime")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to schedule alarm due to permission issue", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm for task: ${task.title}", e)
        }
    }

    /**
     * Cancels an existing alarm for the given task.
     * This should be called when a task is deleted or when updating a task's reminder.
     */
    fun cancelTaskReminder(taskId: Long) {
        try {
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = createPendingIntent(taskId, intent)
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            
            Log.d(TAG, "Cancelled reminder for task ID: $taskId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel alarm for task ID: $taskId", e)
        }
    }

    /**
     * Updates a task reminder by canceling the old alarm and scheduling a new one.
     * This is used when a task's reminder settings or timing are modified.
     */
    fun updateTaskReminder(task: Task) {
        cancelTaskReminder(task.id)
        scheduleTaskReminder(task)
    }

    /**
     * Creates an Intent with all necessary data for the notification.
     */
    private fun createNotificationIntent(task: Task): Intent {
        return Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NotificationReceiver.EXTRA_TASK_ID, task.id)
            putExtra(NotificationReceiver.EXTRA_TASK_TITLE, task.title)
            putExtra(NotificationReceiver.EXTRA_TASK_LOCATION, task.location)
            putExtra(NotificationReceiver.EXTRA_TASK_START_TIME, task.startTime.format(timeFormatter))
        }
    }

    /**
     * Creates a unique PendingIntent for the given task.
     * The request code is based on the task ID to ensure uniqueness.
     */
    private fun createPendingIntent(taskId: Long, intent: Intent): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            taskId.toInt(), // Use task ID as request code for uniqueness
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Checks if exact alarms can be scheduled on this device.
     * This is required for Android 12+ devices.
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * Returns an Intent to request exact alarm permission on Android 12+.
     * This should be used if canScheduleExactAlarms() returns false.
     */
    fun getExactAlarmPermissionIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            }
        } else {
            null
        }
    }
}