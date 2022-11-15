package ua.edu.znu.hitonoriol.aweather.model

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ua.edu.znu.hitonoriol.aweather.model.data.HourlyWeatherForecast
import ua.edu.znu.hitonoriol.aweather.model.data.WeatherForecast

class WeatherService (private val apiKey: String) {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val service = retrofit.create(WeatherRequest::class.java)

    fun fetchHourlyForecast(latitude: Float, longitude: Float): Call<HourlyWeatherForecast> {
        return service.fetchHourly(apiKey, latitude, longitude)
    }
}