package com.firexrwtinc.mytime.ui

// import androidx.lifecycle.asLiveData // Используем Flow напрямую или преобразуем в другом месте, если нужно LiveData для Java
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.firexrwtinc.mytime.data.database.AppDatabase
import com.firexrwtinc.mytime.data.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val taskDao = AppDatabase.getDatabase(application).taskDao()

    val allTasksFlow: Flow<List<Task>> = taskDao.getAllTasks()

    // LiveData для хранения задач на конкретную дату, если используется observeAsState из runtime-livedata
    private val _tasksForDateLiveData = MutableLiveData<List<Task>>()
    val tasksForDateLiveData: LiveData<List<Task>> get() = _tasksForDateLiveData

    // Flow для задач на конкретную дату (предпочтительно для Compose с collectAsStateWithLifecycle)
    // private val _tasksForDateFlow = MutableStateFlow<List<Task>>(emptyList())
    // val tasksForDateFlow: StateFlow<List<Task>> = _tasksForDateFlow.asStateFlow()


    private val _selectedTask = MutableLiveData<Task?>() // LiveData для совместимости или если уже используется
    val selectedTask: LiveData<Task?> get() = _selectedTask

    fun insertTask(task: Task) = viewModelScope.launch {
        taskDao.insertTask(task)
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        taskDao.updateTask(task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        taskDao.deleteTask(task)
    }

    fun loadTasksForDate(date: LocalDate) {
        viewModelScope.launch {
            // Если используете LiveData для tasksForDateLiveData
            taskDao.getTasksForDate(date).collect { tasks ->
                _tasksForDateLiveData.postValue(tasks)
            }
            // Если решите использовать _tasksForDateFlow:
            // taskDao.getTasksForDate(date).collect { tasks ->
            //     _tasksForDateFlow.value = tasks
            // }
        }
    }

    fun loadTaskById(taskId: Long) {
        if (taskId == 0L) {
            _selectedTask.postValue(null)
            return
        }
        viewModelScope.launch {
            taskDao.getTaskById(taskId).collect { task ->
                _selectedTask.postValue(task)
            }
        }
    }

    fun clearSelectedTask() {
        _selectedTask.postValue(null)
    }

    private val _tasksForWeek = MutableStateFlow<Map<LocalDate, List<Task>>>(emptyMap())
    val tasksForWeek: StateFlow<Map<LocalDate, List<Task>>> = _tasksForWeek.asStateFlow()

    fun loadTasksForWeek(dateInWeek: LocalDate) {
        viewModelScope.launch {
            // Определяем начало и конец недели относительно dateInWeek
            val firstDayOfWeekDevice = java.time.temporal.WeekFields.of(Locale.getDefault()).firstDayOfWeek
            val startOfWeek = dateInWeek.with(java.time.temporal.TemporalAdjusters.previousOrSame(firstDayOfWeekDevice))
            val endOfWeek = startOfWeek.plusDays(6) // Неделя - 7 дней

            // Используем существующий метод из DAO, который принимает LocalDate
            taskDao.getTasksForDateRange(startOfWeek, endOfWeek).collect { tasks ->
                // Группируем задачи по дням. Task.date уже является LocalDate.
                _tasksForWeek.value = tasks.groupBy { task -> task.date }
            }
        }
    }

    /**
     * Фабрика для создания TaskViewModel с передачей Application.
     */
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