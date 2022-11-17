package ua.edu.znu.hitonoriol.aweather.model

import android.content.Context
import android.location.Geocoder
import android.widget.Toast
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ua.edu.znu.hitonoriol.aweather.model.data.DailyForecast
import ua.edu.znu.hitonoriol.aweather.model.data.HourlyWeatherForecast
import ua.edu.znu.hitonoriol.aweather.model.data.WeatherForecast
import ua.edu.znu.hitonoriol.aweather.util.execute

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

    private var lastUpdate: Long = 0
    private var currentWeather: WeatherForecast? = null
    private var dailyForecast: DailyForecast? = null
    private var hourlyForecast: HourlyWeatherForecast? = null

    fun setLocation(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
        val locationList = geocoder.getFromLocation(latitude, longitude, 1)
        if (locationList != null && locationList.isNotEmpty()) {
            val location = locationList.first()
            countryName = location.countryName
            cityName =  location.locality
            currentWeather = null
            hourlyForecast = null
        }
    }

    private fun updateFailed() {
        Toast.makeText(context, "Weather update failed.", Toast.LENGTH_LONG).show()
        println("* Weather update failed")
    }

    private fun update(onSuccess: () -> Unit, onFailure: () -> Unit = {}) {
        fetchCurrentWeather()
            .execute({ weather ->
                if (weather == null) {
                    updateFailed()
                    onFailure()
                    return@execute
                }
                currentWeather = weather
                fetchHourlyForecast().execute({ forecast ->
                    if (forecast == null) {
                        updateFailed()
                        onFailure()
                    } else {
                        lastUpdate = System.currentTimeMillis()
                        hourlyForecast = forecast
                        dailyForecast = DailyForecast(hourlyForecast!!)
                        onSuccess()
                    }
                })
            })
    }

    private fun fetchHourlyForecast(): Call<HourlyWeatherForecast> {
        return service.fetchHourly(apiKey, latitude, longitude)
    }

    private fun fetchCurrentWeather(): Call<WeatherForecast> {
        return service.fetchCurrent(apiKey, latitude, longitude)
    }

    private fun <T> getOrUpdate(getter: () -> T?, consumer: (T) -> Unit) {
        val value = getter()
        if (value == null)
            update({ consumer(getter()!!) })
        else
            consumer(value)
    }

    fun getCurrentWeather(action: (WeatherForecast) -> Unit) {
        getOrUpdate({ currentWeather }, action)
    }

    fun getDailyForecast(action: (DailyForecast) -> Unit) {
        getOrUpdate({ dailyForecast }, action)
    }

    fun getHourlyForecast(action: (HourlyWeatherForecast) -> Unit) {
        getOrUpdate({ hourlyForecast }, action)
    }
}