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

    // Search methods
    /**
     * Search tasks by title and description text.
     * Uses case-insensitive LIKE query for flexible text matching.
     * 
     * @param searchQuery The text to search for in title and description
     * @return Flow of tasks matching the search query, ordered by date and time
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE LOWER(title) LIKE '%' || LOWER(:searchQuery) || '%' 
           OR LOWER(description) LIKE '%' || LOWER(:searchQuery) || '%'
        ORDER BY date ASC, startTime ASC
    """)
    fun searchTasks(searchQuery: String): Flow<List<Task>>

    /**
     * Search tasks by title only.
     * 
     * @param titleQuery The text to search for in task titles
     * @return Flow of tasks with matching titles
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE LOWER(title) LIKE '%' || LOWER(:titleQuery) || '%'
        ORDER BY date ASC, startTime ASC
    """)
    fun searchTasksByTitle(titleQuery: String): Flow<List<Task>>

    /**
     * Search tasks by location.
     * 
     * @param locationQuery The text to search for in task locations
     * @return Flow of tasks with matching locations
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE location IS NOT NULL 
          AND LOWER(location) LIKE '%' || LOWER(:locationQuery) || '%'
        ORDER BY date ASC, startTime ASC
    """)
    fun searchTasksByLocation(locationQuery: String): Flow<List<Task>>

    // Filtering methods
    /**
     * Filter tasks by completion status.
     * 
     * @param isCompleted true for completed tasks, false for pending tasks
     * @return Flow of tasks filtered by completion status
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = :isCompleted ORDER BY date ASC, startTime ASC")
    fun getTasksByCompletionStatus(isCompleted: Boolean): Flow<List<Task>>

    /**
     * Get all completed tasks.
     * 
     * @return Flow of completed tasks
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY date ASC, startTime ASC")
    fun getCompletedTasks(): Flow<List<Task>>

    /**
     * Get all pending (uncompleted) tasks.
     * 
     * @return Flow of pending tasks
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY date ASC, startTime ASC")
    fun getPendingTasks(): Flow<List<Task>>

    /**
     * Filter tasks by price range.
     * 
     * @param minPrice Minimum price (inclusive)
     * @param maxPrice Maximum price (inclusive)
     * @return Flow of tasks within the specified price range
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE price IS NOT NULL 
          AND price >= :minPrice 
          AND price <= :maxPrice
        ORDER BY date ASC, startTime ASC
    """)
    fun getTasksByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<Task>>

    /**
     * Get tasks with pricing information only.
     * 
     * @return Flow of tasks that have price set
     */
    @Query("SELECT * FROM tasks WHERE price IS NOT NULL ORDER BY date ASC, startTime ASC")
    fun getTasksWithPrice(): Flow<List<Task>>

    /**
     * Get tasks with reminders set.
     * 
     * @return Flow of tasks that have reminder configured
     */
    @Query("SELECT * FROM tasks WHERE reminderHoursBefore IS NOT NULL ORDER BY date ASC, startTime ASC")
    fun getTasksWithReminders(): Flow<List<Task>>

    // Combined search and filter methods
    /**
     * Search tasks with text query and filter by completion status.
     * 
     * @param searchQuery The text to search for
     * @param isCompleted Filter by completion status
     * @return Flow of tasks matching both criteria
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE (LOWER(title) LIKE '%' || LOWER(:searchQuery) || '%' 
               OR LOWER(description) LIKE '%' || LOWER(:searchQuery) || '%')
          AND isCompleted = :isCompleted
        ORDER BY date ASC, startTime ASC
    """)
    fun searchTasksWithCompletionFilter(searchQuery: String, isCompleted: Boolean): Flow<List<Task>>

    /**
     * Search tasks within a date range.
     * 
     * @param searchQuery The text to search for
     * @param startDate Start date of the range (inclusive)
     * @param endDate End date of the range (inclusive)
     * @return Flow of tasks matching search criteria within date range
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE (LOWER(title) LIKE '%' || LOWER(:searchQuery) || '%' 
               OR LOWER(description) LIKE '%' || LOWER(:searchQuery) || '%')
          AND date BETWEEN :startDate AND :endDate
        ORDER BY date ASC, startTime ASC
    """)
    fun searchTasksInDateRange(searchQuery: String, startDate: LocalDate, endDate: LocalDate): Flow<List<Task>>

    /**
     * Get tasks for a specific date filtered by completion status.
     * 
     * @param date The target date
     * @param isCompleted Filter by completion status
     * @return Flow of tasks for the date with specified completion status
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE date = :date 
          AND isCompleted = :isCompleted
        ORDER BY startTime ASC
    """)
    fun getTasksForDateByStatus(date: LocalDate, isCompleted: Boolean): Flow<List<Task>>

    /**
     * Advanced search with multiple filters.
     * 
     * @param searchQuery Text to search for (can be empty for no text filter)
     * @param startDate Start date filter (nullable)
     * @param endDate End date filter (nullable)
     * @param isCompleted Completion status filter (nullable for no filter)
     * @param hasPrice Whether to include only tasks with price (nullable for no filter)
     * @return Flow of tasks matching all specified criteria
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE (:searchQuery = '' 
               OR LOWER(title) LIKE '%' || LOWER(:searchQuery) || '%' 
               OR LOWER(description) LIKE '%' || LOWER(:searchQuery) || '%')
          AND (:startDate IS NULL OR date >= :startDate)
          AND (:endDate IS NULL OR date <= :endDate)
          AND (:isCompleted IS NULL OR isCompleted = :isCompleted)
          AND (:hasPrice IS NULL OR (:hasPrice = 1 AND price IS NOT NULL) OR (:hasPrice = 0 AND price IS NULL))
        ORDER BY date ASC, startTime ASC
    """)
    fun searchTasksAdvanced(
        searchQuery: String = "",
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        isCompleted: Boolean? = null,
        hasPrice: Boolean? = null
    ): Flow<List<Task>>

    // Utility methods for statistics and aggregation
    /**
     * Count total number of tasks.
     * 
     * @return Flow with total task count
     */
    @Query("SELECT COUNT(*) FROM tasks")
    fun getTotalTaskCount(): Flow<Int>

    /**
     * Count completed tasks.
     * 
     * @return Flow with completed task count
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    fun getCompletedTaskCount(): Flow<Int>

    /**
     * Count pending tasks.
     * 
     * @return Flow with pending task count
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    fun getPendingTaskCount(): Flow<Int>

    /**
     * Get tasks for today (useful for dashboard).
     * 
     * @param today Current date
     * @return Flow of today's tasks
     */
    @Query("SELECT * FROM tasks WHERE date = :today ORDER BY startTime ASC")
    fun getTodaysTasks(today: LocalDate): Flow<List<Task>>

    /**
     * Get upcoming tasks (future dates only).
     * 
     * @param today Current date
     * @return Flow of future tasks
     */
    @Query("SELECT * FROM tasks WHERE date > :today ORDER BY date ASC, startTime ASC")
    fun getUpcomingTasks(today: LocalDate): Flow<List<Task>>

    /**
     * Get overdue incomplete tasks (past dates that are not completed).
     * 
     * @param today Current date
     * @return Flow of overdue tasks
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE date < :today 
          AND isCompleted = 0
        ORDER BY date DESC, startTime ASC
    """)
    fun getOverdueTasks(today: LocalDate): Flow<List<Task>>
}