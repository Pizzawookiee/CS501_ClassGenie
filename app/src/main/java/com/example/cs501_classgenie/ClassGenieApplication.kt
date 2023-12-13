package com.example.cs501_classgenie

import android.app.Application

class ClassGenieApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CalendarRepository.initialize(this)
    }
}