package com.firexrwtinc.mytime.data.database

import androidx.room.*
import com.firexrwtinc.mytime.data.model.TaskTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskTemplateDao {
    @Query("SELECT * FROM task_templates ORDER BY updatedAt DESC")
    fun getAllTemplates(): Flow<List<TaskTemplate>>

    @Query("SELECT * FROM task_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): TaskTemplate?

    @Query("SELECT * FROM task_templates WHERE name LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchTemplates(query: String): Flow<List<TaskTemplate>>

    @Insert
    suspend fun insertTemplate(template: TaskTemplate): Long

    @Update
    suspend fun updateTemplate(template: TaskTemplate)

    @Delete
    suspend fun deleteTemplate(template: TaskTemplate)

    @Query("DELETE FROM task_templates WHERE id = :id")
    suspend fun deleteTemplateById(id: Long)

    @Query("SELECT COUNT(*) FROM task_templates")
    suspend fun getTemplateCount(): Int
}