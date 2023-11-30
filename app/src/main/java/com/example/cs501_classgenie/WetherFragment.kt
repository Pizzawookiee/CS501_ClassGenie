package com.example.cs501_classgenie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.cs501_classgenie.R


class WeatherFragment : Fragment() {

    private val apiKey = "YOUR_API_KEY" // Replace with your actual API key

    // Define a model class for the weather response
    data class WeatherResponse(val main: Main, val weather: List<Weather>) {
        data class Main(val temp: Float, val humidity: Int)
        data class Weather(val description: String)
    }

    // Define the API interface
    interface WeatherApi {
        @GET("data/2.5/weather")
        fun getCurrentWeather(@Query("q") city: String, @Query("appid") apiKey: String): Call<WeatherResponse>
    }

    // Create a Retrofit instance
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://api.openweathermap.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherApi: WeatherApi = retrofit.create(WeatherApi::class.java)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weather, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchWeatherData()
    }

    private fun fetchWeatherData() {
        val call = weatherApi.getCurrentWeather("London", apiKey)
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    // Update UI with the weather data
                    view?.findViewById<TextView>(R.id.textViewTemperature)?.text = "Temperature: ${weatherResponse?.main?.temp}"
                    view?.findViewById<TextView>(R.id.textViewHumidity)?.text = "Humidity: ${weatherResponse?.main?.humidity}%"
                    view?.findViewById<TextView>(R.id.textViewWeatherDescription)?.text = "Weather: ${weatherResponse?.weather?.firstOrNull()?.description}"
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }
}
