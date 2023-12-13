package com.example.cs501_classgenie

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.api.client.util.DateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.util.UUID.randomUUID

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val calendarRepository = CalendarRepository.get()

    fun insertEvent(event: CalendarEvent) = calendarRepository.insertEvent(event)
    fun clearAll() = calendarRepository.clearAll()

    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }


    val _events: MutableStateFlow<List<CalendarEvent>> = MutableStateFlow(
        listOf(CalendarEvent(randomUUID(),
            "dummy",
            DateTime(LocalDateTime.now().plusMinutes(1).toEpochSecond(UTC)),
            DateTime(LocalDateTime.now().plusMinutes(5).toEpochSecond(UTC)),
            "720 commonwealth ave"))
    )
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