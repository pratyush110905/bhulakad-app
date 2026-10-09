package com.example.bhullkad.data.repository

import com.example.bhullkad.data.dao.CalendarEventDao
import com.example.bhullkad.data.dao.NoteDao
import com.example.bhullkad.data.dao.TaskDao
import com.example.bhullkad.data.entity.CalendarEvent
import com.example.bhullkad.data.entity.Note
import com.example.bhullkad.data.entity.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val activeTasks: Flow<List<Task>> = taskDao.observeActive()
    val archivedTasks: Flow<List<Task>> = taskDao.observeArchived()

    suspend fun addTask(title: String): Boolean {
        val clean = title.trim()
        if (clean.isEmpty()) return false
        taskDao.insert(Task(title = clean))
        return true
    }

    suspend fun setCompleted(task: Task, completed: Boolean) {
        taskDao.setCompleted(
            id = task.id,
            completed = completed,
            completedAt = if (completed) System.currentTimeMillis() else null,
        )
    }

    suspend fun deleteTask(task: Task) = taskDao.delete(task)
    suspend fun restoreTask(task: Task) = taskDao.insert(task)
    suspend fun archiveCompletedBefore(cutoff: Long): Int = taskDao.archiveCompletedBefore(cutoff)
    suspend fun clearArchive(): Int = taskDao.clearArchive()
}

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: Flow<List<Note>> = noteDao.observeAll()
    suspend fun getById(id: Long): Note? = noteDao.getById(id)
    suspend fun insert(note: Note): Long = noteDao.insert(note)
    suspend fun update(note: Note) = noteDao.update(note)
    suspend fun delete(note: Note) = noteDao.delete(note)
}

class CalendarEventRepository(private val dao: CalendarEventDao) {
    fun eventsForDate(date: String): Flow<List<CalendarEvent>> = dao.observeByDate(date)
    fun eventsForMonth(monthPrefix: String): Flow<List<CalendarEvent>> = dao.observeByMonth(monthPrefix)
    suspend fun insert(event: CalendarEvent): Long = dao.insert(event)
    suspend fun delete(event: CalendarEvent) = dao.delete(event)
}
