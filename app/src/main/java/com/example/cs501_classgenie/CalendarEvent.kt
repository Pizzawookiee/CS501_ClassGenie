package com.example.cs501_classgenie


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.api.client.util.DateTime
import java.util.UUID
import java.util.Date


@Entity
data class CalendarEvent (
    @PrimaryKey val id: UUID,
    val summary: String,
    val start: DateTime,
    val end: DateTime,
    val location: String?
)