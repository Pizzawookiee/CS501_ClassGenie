package com.example.cs501_classgenie.database

import androidx.room.Dao
import androidx.room.Query
import com.example.cs501_classgenie.CalendarEvent
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface EventDAO {
    @Query("SELECT * FROM CalendarEvent")
    fun getEvents(): Flow<List<CalendarEvent>>
    @Query("SELECT * FROM CalendarEvent WHERE id=(:id)")
    suspend fun getEvent(id: UUID): CalendarEvent

}