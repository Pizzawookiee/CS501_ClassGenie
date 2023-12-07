package com.example.cs501_classgenie


import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID
import java.util.Date


@Entity
data class CalendarEvent (
    @PrimaryKey val id: UUID,
    val summary: String,
    val start: Date,
    val end: Date,
    val location: String?
)