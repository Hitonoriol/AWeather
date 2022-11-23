package ua.edu.znu.hitonoriol.aweather

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import ua.edu.znu.hitonoriol.aweather.databinding.ActivityWeatherBinding
import ua.edu.znu.hitonoriol.aweather.databinding.DayForecastRowBinding
import ua.edu.znu.hitonoriol.aweather.databinding.HourlyForecastCardBinding
import ua.edu.znu.hitonoriol.aweather.databinding.HourlyForecastDividerBinding
import ua.edu.znu.hitonoriol.aweather.model.WeatherService
import ua.edu.znu.hitonoriol.aweather.model.data.DailyForecast
import ua.edu.znu.hitonoriol.aweather.model.data.HourlyWeatherForecast
import ua.edu.znu.hitonoriol.aweather.model.data.WeatherForecast
import ua.edu.znu.hitonoriol.aweather.util.TimeUtils
import ua.edu.znu.hitonoriol.aweather.util.capitalizeFirst
import ua.edu.znu.hitonoriol.aweather.util.getPrefs
import ua.edu.znu.hitonoriol.aweather.util.getStringPreference
import java.time.format.TextStyle
import java.util.*

class WeatherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWeatherBinding
    private lateinit var weatherService: WeatherService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        weatherService = WeatherService(resources.getString(R.string.owm_api_key), this)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onResume() {
        super.onResume()
        Log.d(localClassName, "Weather onResume()")
        val country = getStringPreference(R.string.pref_country)
        val city = getStringPreference(R.string.pref_city)
        if (country == null || city == null) {
            Log.i(localClassName, "First launch! Switching to location selection activity.")
            switchToLocationSelection()
        } else {
            Log.i(localClassName, "Restoring saved location from previous launch...")
            lifecycleScope.launch(Dispatchers.IO) {
                weatherService.restoreLocation()
                refresh()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_scrolling, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.locationBtn -> {
                switchToLocationSelection()
                return true
            }
            R.id.resetAppBtn -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    weatherService.reset()
                    Snackbar.make(binding.root,
                        "Reset application database and preferences successfully!",
                        Snackbar.LENGTH_LONG).show()
                }
                return true
            }
            else -> false
        }
    }

    private fun switchToLocationSelection() {
        startActivity(Intent(this, LocationSelectionActivity::class.java))
    }

    private fun setCityName(name: String) {
        binding.cityNameView.text = name
        binding.toolbarLayout.title = name
    }

    /**
     * Request an update from `weatherService` and update all views with retrieved weather data.
     */
    private fun refresh() {
        Snackbar.make(binding.root, "Refreshing weather data...", Snackbar.LENGTH_LONG).show()
        weatherService.update({ weather ->
            runOnUiThread {
                refreshCurrentWeather(weather.currentForecast!!)
                refreshHourlyForecast(weather.hourlyForecast!!)
                refreshDailyForecast(weather.dailyForecast!!)
                Snackbar.make(binding.root, "Weather data updated!", Snackbar.LENGTH_LONG).show()
            }
        })
    }

    /**
     * Update current weather info in collapsible title layout using the `weather` instance.
     */
    private fun refreshCurrentWeather(weather: WeatherForecast) {
        Log.d(localClassName, "Populating current weather layout with: $weather")
        val condition = weather.mainWeatherCondition
        setCityName(weatherService.cityName)
        binding.weatherConditionImg.setImageResource(getWeatherIcon(condition.icon))
        binding.temperatureView.text = getString(R.string.temp, weather.main?.temp)
        binding.weatherConditionView.text = condition.description.capitalizeFirst()
        binding.pressureView.text = getString(R.string.pressure, weather.main?.pressure)
        binding.windView.text = getString(R.string.wind_speed, weather.wind?.speed)
        binding.humidityView.text = getString(R.string.humidity, weather.main?.humidity)
    }

    private fun getWeatherIcon(name: String): Int {
        val iconName = "w${name.dropLast(1)}"
        return resources.getIdentifier(iconName,"drawable", packageName)
    }

    private fun refreshDailyForecast(forecast: DailyForecast) {
        val table = binding.dailyForecastTable
        table.removeAllViews()
        forecast.days.forEach { (date, day) ->
            Log.d(localClassName, "Creating a new daily forecast row ($date): $day")
            val dayRow = DayForecastRowBinding.inflate(layoutInflater)
            dayRow.dayView.text = day.getString()
            dayRow.dayConditionView.text = day.weatherDescription
            dayRow.dayTempView.text = getString(R.string.temp_min_max, day.minTemp, day.maxTemp)
            dayRow.dayWeatherImg.setImageResource(getWeatherIcon(day.weatherIcon))
            table.addView(dayRow.root)
        }
    }

    private fun refreshHourlyForecast(forecast: HourlyWeatherForecast) {
        val container = binding.hourlyForecastContainer
        container.removeAllViews()
        var prevDay = TimeUtils.localDate(System.currentTimeMillis() / 1000)
        forecast.list?.forEach { hourEntry ->
            val time = TimeUtils.utcDateTime(hourEntry.dt)
            val day = time.toLocalDate()
            if (day != prevDay) {
                val divider = HourlyForecastDividerBinding.inflate(layoutInflater, container, true)
                divider.dividerTextView.text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            }
            val hourCard = HourlyForecastCardBinding.inflate(layoutInflater, container, true)
            hourCard.hourlyTempView.text = getString(R.string.temp_short, hourEntry.main?.temp)
            hourCard.weatherIcon.setImageResource(getWeatherIcon(hourEntry.mainWeatherCondition.icon))
            hourCard.hourlyDayView.text = TimeUtils.timeString(time.toLocalTime())
            prevDay = day
        }
    }
}