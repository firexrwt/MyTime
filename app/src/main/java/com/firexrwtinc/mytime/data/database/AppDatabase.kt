package com.firexrwtinc.mytime.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.firexrwtinc.mytime.data.model.Task

@Database(entities = [Task::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_time_database"
                )
                    // TODO: Рассмотреть миграции при изменении схемы БД в будущем
                    // .addMigrations(MIGRATION_1_2) // Пример
                    .fallbackToDestructiveMigration() // Временно, для разработки. Удаляет и пересоздает БД при изменении схемы.
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}