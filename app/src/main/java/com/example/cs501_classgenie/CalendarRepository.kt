package com.example.cs501_classgenie

import android.content.Context
import androidx.room.Room
import com.example.cs501_classgenie.database.CalendarDatabase
import kotlinx.coroutines.flow.Flow
import java.util.UUID

private const val DATABASE_NAME = "calendar-database"

class CalendarRepository private constructor(context: Context) {
    private val database: CalendarDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            CalendarDatabase::class.java,
            DATABASE_NAME
        )
        .enableMultiInstanceInvalidation()
        .build()

    fun getEvents(): Flow<List<CalendarEvent>> = database.eventDao().getEvents()
    fun getEvent(id: UUID): CalendarEvent = database.eventDao().getEvent(id)

    fun insertEvent(event: CalendarEvent) = database.eventDao().insertEvent(event)
    fun clearAll() = database.eventDao().clearAll()

    companion object {
        private var INSTANCE: CalendarRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CalendarRepository(context)
            }
        }
        fun get(): CalendarRepository {
            return INSTANCE ?:
            throw IllegalStateException("CalendarRepository must be initialized")
        }
    }
}