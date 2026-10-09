package com.example.bhullkad.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String = "",

    /** ISO local date string, e.g. "2026-10-09" */
    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
)
