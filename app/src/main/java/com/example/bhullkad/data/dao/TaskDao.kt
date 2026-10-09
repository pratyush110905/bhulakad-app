package com.example.bhullkad.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.bhullkad.data.entity.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE is_archived = 0 ORDER BY is_completed ASC, created_at ASC")
    fun observeActive(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE is_archived = 1 ORDER BY completed_at DESC")
    fun observeArchived(): Flow<List<Task>>

    @Insert
    suspend fun insert(task: Task): Long

    @Delete
    suspend fun delete(task: Task)

    @Query("UPDATE tasks SET is_completed = :completed, completed_at = :completedAt WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean, completedAt: Long?)

    @Query(
        "UPDATE tasks SET is_archived = 1 WHERE is_completed = 1 AND is_archived = 0 " +
            "AND (completed_at IS NULL OR completed_at < :cutoff)"
    )
    suspend fun archiveCompletedBefore(cutoff: Long): Int

    @Query("DELETE FROM tasks WHERE is_archived = 1")
    suspend fun clearArchive(): Int
}
