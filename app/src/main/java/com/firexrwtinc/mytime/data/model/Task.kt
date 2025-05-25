package com.firexrwtinc.mytime.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var title: String,
    var date: LocalDate,
    var startTime: LocalTime,
    var endTime: LocalTime,
    var location: String? = null,
    var equipment: String? = null,
    var reminderHoursBefore: Int? = null,
    var price: Double? = null,
    var colorHex: String,
    var isCompleted: Boolean = false
    // TODO: Рассмотреть добавление поля для описания задачи (если одного title мало)
    // TODO: Рассмотреть добавление поля для повторяющихся задач
)
