package com.firexrwtinc.mytime.data.database

import androidx.room.TypeConverter
import com.firexrwtinc.mytime.data.model.RecurrenceType
import com.firexrwtinc.mytime.data.model.ThemeMode
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Converters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(dateFormatter)
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, dateFormatter) }
    }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? {
        return time?.format(timeFormatter)
    }

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it, timeFormatter) }
    }

    @TypeConverter
    fun fromRecurrenceType(recurrenceType: RecurrenceType): String {
        return recurrenceType.name
    }

    @TypeConverter
    fun toRecurrenceType(value: String): RecurrenceType {
        return try {
            RecurrenceType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            RecurrenceType.NONE
        }
    }

    @TypeConverter
    fun fromThemeMode(themeMode: ThemeMode): String {
        return themeMode.name
    }

    @TypeConverter
    fun toThemeMode(value: String): ThemeMode {
        return try {
            ThemeMode.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }
}