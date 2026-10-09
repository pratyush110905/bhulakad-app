package com.example.bhullkad.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.bhullkad.data.entity.CalendarEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDao {

    @Query("SELECT * FROM events WHERE date = :date ORDER BY created_at ASC")
    fun observeByDate(date: String): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM events WHERE date LIKE :monthPrefix || '%'")
    fun observeByMonth(monthPrefix: String): Flow<List<CalendarEvent>>

    @Insert
    suspend fun insert(event: CalendarEvent): Long

    @Delete
    suspend fun delete(event: CalendarEvent)
}
