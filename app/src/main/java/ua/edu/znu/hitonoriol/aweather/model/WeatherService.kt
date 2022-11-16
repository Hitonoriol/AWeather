package ua.edu.znu.hitonoriol.aweather.model

import android.content.Context
import android.location.Geocoder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ua.edu.znu.hitonoriol.aweather.model.data.HourlyWeatherForecast
import ua.edu.znu.hitonoriol.aweather.model.data.WeatherForecast

class WeatherService (private val apiKey: String, private val context: Context) {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val service = retrofit.create(WeatherRequest::class.java)
    private val geocoder = Geocoder(context)

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    var cityName: String = ""
    var countryName: String = ""

    fun setLocation(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
        val locationList = geocoder.getFromLocation(latitude, longitude, 1)
        if (locationList != null && locationList.isNotEmpty()) {
            val location = locationList.first()
            countryName = location.countryName
            cityName =  location.locality
        }
    }

    fun fetchHourlyForecast(): Call<HourlyWeatherForecast> {
        return service.fetchHourly(apiKey, latitude, longitude)
    }

    fun fetchCurrentWeather(): Call<WeatherForecast> {
        return service.fetchCurrent(apiKey, latitude, longitude)
    }
}