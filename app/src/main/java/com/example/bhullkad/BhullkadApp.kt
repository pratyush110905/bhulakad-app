package com.example.bhullkad

import android.app.Application
import com.example.bhullkad.data.AppDatabase
import com.example.bhullkad.data.UserPreferences
import com.example.bhullkad.data.repository.CalendarEventRepository
import com.example.bhullkad.data.repository.NoteRepository
import com.example.bhullkad.data.repository.TaskRepository
import com.example.bhullkad.reset.DailyResetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BhullkadApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val preferences: UserPreferences by lazy { UserPreferences(this) }

    val taskRepository: TaskRepository by lazy { TaskRepository(database.taskDao()) }
    val noteRepository: NoteRepository by lazy { NoteRepository(database.noteDao()) }
    val calendarEventRepository: CalendarEventRepository by lazy {
        CalendarEventRepository(database.calendarEventDao())
    }

    val resetManager: DailyResetManager by lazy {
        DailyResetManager(this, taskRepository, preferences)
    }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            resetManager.syncNow()
        }
    }
}
