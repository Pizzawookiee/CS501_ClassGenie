package com.example.cs501_classgenie.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.cs501_classgenie.CalendarEvent
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface EventDAO {
    @Query("DELETE FROM CalendarEvent")
    fun clearAll()

    @Query("SELECT * FROM CalendarEvent")
    fun getEvents(): Flow<List<CalendarEvent>>
    @Query("SELECT * FROM CalendarEvent WHERE id=(:id)")
    fun getEvent(id: UUID): CalendarEvent

    @Insert
    fun insertEvent(user: CalendarEvent)

}