package com.firexrwtinc.mytime.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.firexrwtinc.mytime.data.database.AppDatabase
import com.firexrwtinc.mytime.data.model.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale
import com.firexrwtinc.mytime.notifications.AlarmManagerHelper
import com.firexrwtinc.mytime.notifications.NotificationScheduler

@OptIn(ExperimentalCoroutinesApi::class) // Для flatMapLatest
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val taskDao = AppDatabase.getDatabase(application).taskDao()
    private val alarmManagerHelper = AlarmManagerHelper(application)
    private val notificationScheduler = NotificationScheduler(application)

    val allTasksFlow: Flow<List<Task>> = taskDao.getAllTasks()

    private val _currentDayScreenDate = MutableStateFlow(LocalDate.now())

    val tasksForDateFlow: StateFlow<List<Task>> = _currentDayScreenDate.flatMapLatest { date ->
        taskDao.getTasksForDate(date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )
    fun setCurrentDayForObserver(newDate: LocalDate) {
        _currentDayScreenDate.value = newDate
    }
    private val _selectedTask = MutableLiveData<Task?>()
    val selectedTask: LiveData<Task?> get() = _selectedTask

    fun insertTask(task: Task) = viewModelScope.launch {
        try {
            taskDao.insertTask(task)
            notificationScheduler.scheduleNotificationForTask(task)
        } catch (e: Exception) {
            android.util.Log.e("TaskViewModel", "Error inserting task", e)
        }
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        try {
            taskDao.updateTask(task)
            notificationScheduler.cancelNotificationForTask(task.id)
            notificationScheduler.scheduleNotificationForTask(task)
        } catch (e: Exception) {
            android.util.Log.e("TaskViewModel", "Error updating task", e)
        }
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        try {
            notificationScheduler.cancelNotificationForTask(task.id)
            taskDao.deleteTask(task)
        } catch (e: Exception) {
            android.util.Log.e("TaskViewModel", "Error deleting task", e)
        }
    }

    fun loadTaskById(taskId: Long) {
        if (taskId == 0L) {
            _selectedTask.postValue(null)
            return
        }
        viewModelScope.launch {
            taskDao.getTaskById(taskId).collect { task -> // Используем collect для Flow
                _selectedTask.postValue(task)
            }
        }
    }

    fun clearSelectedTask() {
        _selectedTask.postValue(null)
    }

    // --- Логика для WeekScreen (остается как есть) ---
    private val _tasksForWeek = MutableStateFlow<Map<LocalDate, List<Task>>>(emptyMap())
    val tasksForWeek: StateFlow<Map<LocalDate, List<Task>>> = _tasksForWeek.asStateFlow()

    fun loadTasksForWeek(dateInWeek: LocalDate) {
        viewModelScope.launch {
            val firstDayOfWeekDevice = java.time.temporal.WeekFields.of(Locale.getDefault()).firstDayOfWeek
            val startOfWeek = dateInWeek.with(java.time.temporal.TemporalAdjusters.previousOrSame(firstDayOfWeekDevice))
            val endOfWeek = startOfWeek.plusDays(6)

            taskDao.getTasksForDateRange(startOfWeek, endOfWeek).collect { tasks ->
                _tasksForWeek.value = tasks.groupBy { task -> task.date }
            }
        }
    }
    
    fun rescheduleAllNotifications() = viewModelScope.launch {
        try {
            notificationScheduler.rescheduleAllTasks()
        } catch (e: Exception) {
            android.util.Log.e("TaskViewModel", "Error rescheduling notifications", e)
        }
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
