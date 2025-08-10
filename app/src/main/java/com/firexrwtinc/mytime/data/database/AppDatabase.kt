package com.firexrwtinc.mytime.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.firexrwtinc.mytime.data.model.Task
import com.firexrwtinc.mytime.data.model.TaskTemplate
import com.firexrwtinc.mytime.data.model.AppSettings

@Database(entities = [Task::class, TaskTemplate::class, AppSettings::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun taskTemplateDao(): TaskTemplateDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Добавляем новые поля в таблицу tasks
                database.execSQL("ALTER TABLE tasks ADD COLUMN description TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE tasks ADD COLUMN recurrenceType TEXT NOT NULL DEFAULT 'NONE'")
                database.execSQL("ALTER TABLE tasks ADD COLUMN recurrenceEndDate TEXT")
                
                // Создаем таблицу task_templates
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS task_templates (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        location TEXT NOT NULL DEFAULT '',
                        equipment TEXT NOT NULL DEFAULT '',
                        colorHex TEXT NOT NULL,
                        defaultDurationMinutes INTEGER NOT NULL DEFAULT 60,
                        notificationMinutesBefore INTEGER NOT NULL DEFAULT 60,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Создаем таблицу app_settings
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS app_settings (
                        id INTEGER PRIMARY KEY NOT NULL,
                        timeFormat24Hour INTEGER NOT NULL DEFAULT 1,
                        themeMode TEXT NOT NULL DEFAULT 'SYSTEM',
                        accentColor TEXT NOT NULL DEFAULT '#FF6200EA',
                        defaultNotificationMinutes INTEGER NOT NULL DEFAULT 60,
                        enableNotifications INTEGER NOT NULL DEFAULT 1,
                        language TEXT NOT NULL DEFAULT 'ru',
                        showCompletedTasks INTEGER NOT NULL DEFAULT 1,
                        autoDeleteCompletedTasksAfterDays INTEGER NOT NULL DEFAULT 30,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Добавляем поля координат в таблицу tasks
                database.execSQL("ALTER TABLE tasks ADD COLUMN locationLatitude REAL")
                database.execSQL("ALTER TABLE tasks ADD COLUMN locationLongitude REAL")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_time_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration() // Временно, для разработки
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}