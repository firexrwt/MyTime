package com.firexrwtinc.mytime.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.firexrwtinc.mytime.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: Long): Flow<Task?>

    @Query("SELECT * FROM tasks ORDER BY date ASC, startTime ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY startTime ASC")
    fun getTasksForDate(date: LocalDate): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, startTime ASC")
    fun getTasksForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Task>>

    // TODO: Добавить методы для поиска задач, фильтрации по статусу и т.д.
}