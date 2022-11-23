package ua.edu.znu.hitonoriol.aweather.model

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ua.edu.znu.hitonoriol.aweather.R
import ua.edu.znu.hitonoriol.aweather.model.data.DailyForecast
import ua.edu.znu.hitonoriol.aweather.model.data.HourlyWeatherForecast
import ua.edu.znu.hitonoriol.aweather.model.data.WeatherForecast
import ua.edu.znu.hitonoriol.aweather.persist.LocalWeather
import ua.edu.znu.hitonoriol.aweather.persist.WeatherDatabase
import ua.edu.znu.hitonoriol.aweather.util.execute
import ua.edu.znu.hitonoriol.aweather.util.getDoublePreference
import ua.edu.znu.hitonoriol.aweather.util.getPrefs
import ua.edu.znu.hitonoriol.aweather.util.getStringPreference

/**
 * Worker class that is responsible for supplying its owner with the latest weather data for the
 * location saved is the app's SharedPreferences. Communicates with Openweathermap API as needed.
 */
class WeatherService(private val apiKey: String, private val context: Context) {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val service = retrofit.create(WeatherRequest::class.java)
    private val db = WeatherDatabase.initialize(context)

    private var weather = LocalWeather()
    val cityName: String
        get() = weather.city

    /**
     * Restore the last saved location and initialize the `weather: LocalWeather` instance.
     */
    suspend fun restoreLocation() = coroutineScope {
        val city = context.getStringPreference(R.string.pref_city)!!
        val country = context.getStringPreference(R.string.pref_country)!!

        /* Restore previously fetched weather data from the database, if it exists.
         * Otherwise, only set `weather`'s location to the saved one. */
        val previousWeather = db.weatherDao().findByLocation(city, country)
        if (previousWeather != null)
            weather = previousWeather
        else {
            val latitude = context.getDoublePreference(R.string.pref_lat)
            val longitude = context.getDoublePreference(R.string.pref_lon)
            weather.setLocation(city, country, latitude, longitude)
            weather.invalidate()
        }
    }

    /**
     * Reset the saved city and all previously fetched weather data.
     */
    suspend fun reset() = coroutineScope {
        db.weatherDao().purge()
        context.getPrefs().edit().clear().apply()
    }

    /**
     * Default action that is called on update failure before calling the user-supplied action.
     */
    private fun updateFailed() {
        Toast.makeText(context, "Weather update failed.", Toast.LENGTH_LONG).show()
        Log.w(TAG, "Weather update failed")
    }

    /**
     * Check whether current weather data needs to be updated or not.
     * Returns true if one of the following checks is true:
     *  1. Data is invalid (at least one of the internal forecast objects is null)
     *  2. Data is older than `integer.update_interval`
     */
    private fun needsUpdate(): Boolean {
        if (!weather.valid)
            return true

        val sinceUpdate = (System.currentTimeMillis() - weather.lastUpdate) / 1000
        val updateInterval = context.resources.getInteger(R.integer.update_interval)
        return sinceUpdate >= updateInterval
    }

    /**
     * Retrieve weather data from Openweathermap API or reuse the old data if it's still valid and
     *  pass it to `onSuccess` consumer.
     * If any of the requests fail or the API returns an empty response, `onFailure` will be called.
     */
    fun update(
        onSuccess: (LocalWeather) -> Unit,
        onFailure: () -> Unit = {}
    ) {

        if (!needsUpdate()) {
            Log.d(
                TAG, "Update was requested, but previous data is still valid " +
                        "so no API requests will be performed"
            )
            onSuccess(this.weather)
            return
        }

        Log.d(TAG, "Fetching weather data from the API...")
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
                        runBlocking {
                            db.weatherDao().persist(this@WeatherService.weather)
                        }
                        onSuccess(this.weather)
                    }
                })
            })
    }

    /**
     * Form an hourly weather forecast request to Openweathermap API.
     */
    private fun fetchHourlyForecast(): Call<HourlyWeatherForecast> {
        return service.fetchHourly(apiKey, weather.latitude, weather.longitude)
    }

    /**
     * Form a current weather forecast request to Openweathermap API.
     */
    private fun fetchCurrentWeather(): Call<WeatherForecast> {
        return service.fetchCurrent(apiKey, weather.latitude, weather.longitude)
    }

    companion object {
        private const val TAG = "WeatherService"
    }
}