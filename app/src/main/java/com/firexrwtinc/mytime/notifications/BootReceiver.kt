package com.firexrwtinc.mytime.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("BootReceiver", "Received action: ${intent.action}")
        
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Log.d("BootReceiver", "Device booted or app updated, rescheduling alarms")
                
                // Перепланируем все уведомления
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val notificationScheduler = NotificationScheduler(context)
                        notificationScheduler.rescheduleAllTasks()
                        Log.d("BootReceiver", "All alarms rescheduled successfully")
                    } catch (e: Exception) {
                        Log.e("BootReceiver", "Error rescheduling alarms", e)
                    }
                }
            }
        }
    }
}