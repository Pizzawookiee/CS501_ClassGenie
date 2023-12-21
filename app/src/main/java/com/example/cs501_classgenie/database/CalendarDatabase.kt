package com.example.cs501_classgenie.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.cs501_classgenie.CalendarEvent
import java.util.Date


//Database for offline caching (allows for notifications to continue even if internet is down)
@Database(entities = [ CalendarEvent::class ], version=1, exportSchema=false)
@TypeConverters(EventTypeConverters::class)
abstract class CalendarDatabase : RoomDatabase(){

    abstract fun eventDao(): EventDAO

}