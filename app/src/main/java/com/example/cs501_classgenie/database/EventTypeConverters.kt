package com.example.cs501_classgenie.database

import androidx.room.TypeConverter
import java.util.Date

class EventTypeConverters {
    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }
    @TypeConverter
    fun toDate(millisSinceEpoch: Long): Date {
        return Date(millisSinceEpoch)
    }
}