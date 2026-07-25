package com.firexrwtinc.mytime.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.firexrwtinc.mytime.data.database.AppDatabase
import com.firexrwtinc.mytime.data.model.Task
import com.firexrwtinc.mytime.notifications.NotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class) // Для flatMapLatest
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val taskDao = AppDatabase.getDatabase(application).taskDao()
    private val notificationHelper = NotificationHelper(application)

    val allTasksFlow: Flow<List<Task>> = taskDao.getAllTasks()

    private val _currentDayScreenDate = MutableStateFlow(LocalDate.now())

    val tasksForDateFlow: StateFlow<List<Task>> = _currentDayScreenDate.flatMapLatest { date ->
        taskDao.getTasksForDate(date)
    }.stateIn(
        scope = viewModelScope,
        // WhileSubscribed: drops the Room observer when no screen is watching (e.g. app in
        // background) instead of keeping a cursor open until onCleared.
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )
    fun setCurrentDayForObserver(newDate: LocalDate) {
        _currentDayScreenDate.value = newDate
    }
    private val _selectedTaskId = MutableStateFlow(0L)
    val selectedTask: LiveData<Task?> = _selectedTaskId
        .flatMapLatest { id -> if (id == 0L) flowOf(null) else taskDao.getTaskById(id) }
        .asLiveData()

    fun insertTask(task: Task) = viewModelScope.launch {
        taskDao.insertTask(task)
        // Schedule notification if reminder is set (don't let this break task saving)
        try {
            notificationHelper.scheduleTaskReminder(task)
        } catch (e: Exception) {
            // Log error but don't break task saving
            android.util.Log.e("TaskViewModel", "Failed to schedule notification", e)
        }
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        taskDao.updateTask(task)
        // Update notification (cancel old and schedule new if needed)
        try {
            notificationHelper.updateTaskReminder(task)
        } catch (e: Exception) {
            // Log error but don't break task updating
            android.util.Log.e("TaskViewModel", "Failed to update notification", e)
        }
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        // Cancel notification before deleting task
        try {
            notificationHelper.cancelTaskReminder(task.id)
        } catch (e: Exception) {
            // Log error but don't break task deletion
            android.util.Log.e("TaskViewModel", "Failed to cancel notification", e)
        }
        taskDao.deleteTask(task)
    }

    fun loadTaskById(taskId: Long) {
        _selectedTaskId.value = taskId
    }

    fun clearSelectedTask() {
        _selectedTaskId.value = 0L
    }

    // --- Логика для WeekScreen ---
    private val _currentWeekDate = MutableStateFlow(LocalDate.now())
    val tasksForWeek: StateFlow<Map<LocalDate, List<Task>>> = _currentWeekDate
        .flatMapLatest { dateInWeek ->
            val firstDayOfWeekDevice = java.time.temporal.WeekFields.of(Locale.getDefault()).firstDayOfWeek
            val startOfWeek = dateInWeek.with(java.time.temporal.TemporalAdjusters.previousOrSame(firstDayOfWeekDevice))
            val endOfWeek = startOfWeek.plusDays(6)
            taskDao.getTasksForDateRange(startOfWeek, endOfWeek)
        }
        .map { tasks -> tasks.groupBy { task -> task.date } }
        .stateIn(
            scope = viewModelScope,
            // WhileSubscribed: drops the Room observer when no screen is watching (e.g. app in
        // background) instead of keeping a cursor open until onCleared.
        started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )

    fun loadTasksForWeek(dateInWeek: LocalDate) {
        _currentWeekDate.value = dateInWeek
    }

    // --- Notification system methods ---

    /**
     * Checks if the app can schedule exact alarms (required for reliable notifications).
     * This is particularly important for Android 12+ devices.
     */
    fun canScheduleExactAlarms(): Boolean {
        return notificationHelper.canScheduleExactAlarms()
    }

    /**
     * Returns an Intent to request exact alarm permission for Android 12+ devices.
     * Returns null for older Android versions where this permission is not needed.
     */
    fun getExactAlarmPermissionIntent(): android.content.Intent? {
        return notificationHelper.getExactAlarmPermissionIntent()
    }

    class TaskViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TaskViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
