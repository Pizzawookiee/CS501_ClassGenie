package com.example.cs501_classgenie

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.CalendarScopes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader


class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val calendarRepository = CalendarRepository.get()

    fun insertEvent(event: CalendarEvent) = calendarRepository.insertEvent(event)
    /*
    fun refresh_database(){
        viewModelScope.launch{
            calendarRepository.getEvents().collect {
                _events.value = it
            }
        }
    }

     */


    val _events: MutableStateFlow<List<CalendarEvent>> = MutableStateFlow(emptyList())
    val events: StateFlow<List<CalendarEvent>>
        get() = _events.asStateFlow()
    init{
        viewModelScope.launch{
            calendarRepository.getEvents().collect {
                _events.value = it
            }
        }
    }




}