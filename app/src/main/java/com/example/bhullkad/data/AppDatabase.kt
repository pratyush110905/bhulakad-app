package com.example.bhullkad.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bhullkad.data.dao.CalendarEventDao
import com.example.bhullkad.data.dao.NoteDao
import com.example.bhullkad.data.dao.TaskDao
import com.example.bhullkad.data.entity.CalendarEvent
import com.example.bhullkad.data.entity.Note
import com.example.bhullkad.data.entity.Task

@Database(
    entities = [Task::class, Note::class, CalendarEvent::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
    abstract fun calendarEventDao(): CalendarEventDao

    companion object {
        private const val DB_NAME = "bhullkad.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME,
                ).build().also { instance = it }
            }
    }
}
