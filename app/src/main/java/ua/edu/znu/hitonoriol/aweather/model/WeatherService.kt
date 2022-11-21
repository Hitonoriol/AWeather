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
import ua.edu.znu.hitonoriol.aweather.persist.LocalWeather
import ua.edu.znu.hitonoriol.aweather.persist.WeatherDatabase
import ua.edu.znu.hitonoriol.aweather.util.execute

class WeatherService (private val apiKey: String, private val context: Context) {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val service = retrofit.create(WeatherRequest::class.java)
    private val geocoder = Geocoder(context)
    private val db = WeatherDatabase.initialize(context)

    private val weather = LocalWeather()
    val cityName: String
        get() = weather.city

    fun setLocation(latitude: Double, longitude: Double) {
        val locationList = geocoder.getFromLocation(latitude, longitude, 1)
        if (locationList != null && locationList.isNotEmpty()) {
            val location = locationList.first()
            weather.setLocation(location.locality, location.countryName, latitude, longitude)
            weather.invalidate()
        }
    }

    private fun updateFailed() {
        Toast.makeText(context, "Weather update failed.", Toast.LENGTH_LONG).show()
        println("* Weather update failed")
    }

    private fun needsUpdate(): Boolean {
        return !weather.valid
    }

    fun update(onSuccess: (LocalWeather) -> Unit,
               onFailure: () -> Unit = {}) {
        if (!needsUpdate()) {
            onSuccess(this.weather)
            return
        }

        fetchCurrentWeather()
            .execute({ weather ->
                if (weather == null) {
                    updateFailed()
                    onFailure()
                    return@execute
                }
                this.weather.currentForecast = weather
                fetchHourlyForecast().execute({ forecast ->
                    if (forecast == null) {
                        updateFailed()
                        onFailure()
                    } else {
                        this.weather.lastUpdate = System.currentTimeMillis()
                        this.weather.hourlyForecast = forecast
                        this.weather.dailyForecast = DailyForecast(forecast)
                        db.weatherDao().persist(this.weather)
                        onSuccess(this.weather)
                    }
                })
            })
    }

    private fun fetchHourlyForecast(): Call<HourlyWeatherForecast> {
        return service.fetchHourly(apiKey, weather.latitude, weather.longitude)
    }

    private fun fetchCurrentWeather(): Call<WeatherForecast> {
        return service.fetchCurrent(apiKey, weather.latitude, weather.longitude)
    }
}