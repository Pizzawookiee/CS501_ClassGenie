package com.example.cs501_classgenie.database

import androidx.room.TypeConverter
import com.google.api.client.util.DateTime
import java.util.Date

class EventTypeConverters {
    @TypeConverter
    fun fromDate(date: DateTime): Long {
        return date.value
    }
    @TypeConverter
    fun toDate(millisSinceEpoch: Long): DateTime {
        return DateTime(millisSinceEpoch)
    }
}