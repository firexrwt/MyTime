package com.firexrwtinc.mytime.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.firexrwtinc.mytime.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Re-schedules task reminders after a device reboot or an app update.
 * AlarmManager silently drops every pending alarm on both events, so without
 * this receiver all reminders would just disappear until the user reopens the app.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        val appContext = context.applicationContext
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tasks = AppDatabase.getDatabase(appContext).taskDao()
                    .getTasksWithRemindersFrom(LocalDate.now())
                val notificationHelper = NotificationHelper(appContext)
                tasks.forEach { task ->
                    notificationHelper.scheduleTaskReminder(task)
                }
                Log.d(TAG, "Re-scheduled ${tasks.size} task reminder(s) after ${intent.action}")
            } catch (e: Exception) {
                // Never let a reschedule failure crash the receiver or the app.
                Log.e(TAG, "Failed to re-schedule task reminders", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
