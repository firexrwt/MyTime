package com.firexrwtinc.mytime.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "task_templates")
data class TaskTemplate(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var name: String,
    var description: String = "",
    var location: String = "",
    var equipment: String = "",
    var colorHex: String,
    var defaultDurationMinutes: Int = 60,
    var notificationMinutesBefore: Int = 60,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
)