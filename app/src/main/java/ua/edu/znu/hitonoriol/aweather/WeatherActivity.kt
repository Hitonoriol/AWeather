package ua.edu.znu.hitonoriol.aweather

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ua.edu.znu.hitonoriol.aweather.databinding.ActivityWeatherBinding
import ua.edu.znu.hitonoriol.aweather.databinding.DayForecastRowBinding
import ua.edu.znu.hitonoriol.aweather.databinding.HourlyForecastCardBinding
import ua.edu.znu.hitonoriol.aweather.databinding.HourlyForecastDividerBinding
import ua.edu.znu.hitonoriol.aweather.model.WeatherService
import ua.edu.znu.hitonoriol.aweather.model.data.DailyForecast
import ua.edu.znu.hitonoriol.aweather.model.data.HourlyWeatherForecast
import ua.edu.znu.hitonoriol.aweather.model.data.WeatherForecast
import ua.edu.znu.hitonoriol.aweather.persist.LocalWeather
import ua.edu.znu.hitonoriol.aweather.util.*
import java.net.UnknownHostException
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.*

/**
 * Application's main activity. Displays weather data for the currently selected city.
 */
class WeatherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWeatherBinding
    private lateinit var weatherService: WeatherService

    companion object {
        private const val fadeDuration = 300L
        private const val alphaTransparent = .0f
        private const val alphaOpaque = 1f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        weatherService = WeatherService(resources.getString(R.string.owm_api_key), this)
        setContentView(binding.root)
        setWeatherVisible(false)
        setSupportActionBar(findViewById(R.id.toolbar))
        binding.swipeRefresh.setOnRefreshListener {
            refresh()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e(localClassName, "Weather onResume()")
        val country = getStringPreference(R.string.pref_country)
        val city = getStringPreference(R.string.pref_city)
        if (country == null || city == null) { // Detect first launch
            Log.i(localClassName, "First launch! Switching to location selection activity.")
            switchToLocationSelection()
        } else { // Restore weather data associated with the saved location and populate current layout with it.
            Log.i(localClassName, "Restoring saved location from previous launch...")
            lifecycleScope.launch(Dispatchers.IO) {
                var warmStart = false
                /* Populate the layout with previously fetched data */
                weatherService.restoreLocation { oldData ->
                    warmStart = true
                    runOnUiThread {
                        refresh(oldData)
                        fadeIn()
                    }
                }
                /* Refresh the layout with new data if needed */
                runOnUiThread {
                    /* Fade-out the layout before refreshing only if weather data for this location
                     * has never been fetched before */
                    if (!warmStart)
                        fadeOut { refresh() }
                    else
                        refresh()
                }
            }
        }
    }

    private fun setWeatherVisible(visible: Boolean) {
        val alpha = if (visible) alphaOpaque else alphaTransparent
        binding.toolbarLayout.alpha = alpha
        binding.weatherContainer.alpha = alpha
    }

    private fun setRefreshing(refreshing: Boolean) {
        binding.swipeRefresh.isRefreshing = refreshing
    }

    private fun fadeIn(onFinish: () -> Unit = {}) {
        fade(false, onFinish)
    }

    private fun fadeOut(onFinish: () -> Unit = {}) {
        fade(true, onFinish)
    }

    private fun fade(out: Boolean, onFinish: () -> Unit = {}) {
        fade(binding.toolbarLayout, out, onFinish)
        fade(binding.weatherContainer, out)
    }

    private fun fade(view: View, out: Boolean = true, onFinish: () -> Unit = {}) {
        view.apply {
            val from = if (out) alphaOpaque else alphaTransparent
            val to = if (out) alphaTransparent else alphaOpaque
            if (from == to) {
                onFinish()
                return
            }

            println("Animating $from -> $to (fadeOut: $out)")
            animate()
                .alpha(to)
                .setDuration(fadeDuration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        onFinish()
                        super.onAnimationEnd(animation)
                    }
                })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_scrolling, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.locationBtn -> {
                switchToLocationSelection()
                return true
            }
            R.id.resetAppBtn -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    weatherService.reset()
                    showSnackbar(binding.root, R.string.msg_app_reset)
                }
                return true
            }
            else -> false
        }
    }

    private fun switchToLocationSelection() {
        startActivity(Intent(this, LocationSelectionActivity::class.java))
    }

    /**
     * Request an update from `weatherService` and update all views with retrieved weather data.
     * Old data may be reused (without making any API requests) if it's still valid.
     */
    private fun refresh() {
        Log.e(localClassName, "refresh()")
        setRefreshing(true)
        weatherService.update({ weather ->
            runOnUiThread {
                refresh(weather)
                fadeIn()
                setRefreshing(false)
                showSnackbar(binding.root, R.string.msg_update_success)
            }
        }, { exception ->
            setRefreshing(false)
            if (exception == null) {
                showSnackbar(binding.root, R.string.error_empty_response)
                return@update
            }
            exception.printStackTrace()
            showSnackbar(
                binding.root,
                if (exception is UnknownHostException)
                    R.string.error_no_internet
                else
                    R.string.error_update_unknown
            )
        })
    }

    private fun refresh(weather: LocalWeather) {
        refreshCurrentWeather(weather.currentForecast!!)
        refreshHourlyForecast(weather.currentForecast!!, weather.hourlyForecast!!)
        refreshDailyForecast(weather.dailyForecast!!)
    }

    /**
     * Retrieve resource id of the icon corresponding to the icon code returned by Openweathermap API.
     */
    private fun getWeatherIcon(name: String): Int {
        val iconName = "w${name.dropLast(1)}"
        return resources.getIdentifier(iconName, "drawable", packageName)
    }

    /**
     * Update current weather info in collapsible title layout using the `WeatherForecast` instance.
     */
    private fun refreshCurrentWeather(weather: WeatherForecast) {
        Log.d(localClassName, "Populating current weather layout with: $weather")
        val lastUpdate = TimeUtils.localDateTime(weatherService.lastUpdate / 1000)
        val condition = weather.mainWeatherCondition
        binding.apply {
            toolbar.title = "${weatherService.cityName}, ${weatherService.countryName}"
            toolbar.subtitle = getString(
                R.string.last_update,
                TimeUtils.dateString(lastUpdate.toLocalDate()),
                TimeUtils.timeString(lastUpdate.toLocalTime())
            )
            cityNameView.text = weatherService.cityName
            weatherConditionImg.setImageResource(getWeatherIcon(condition.icon))
            temperatureView.text = getString(R.string.temp, weather.main?.temp)
            weatherConditionView.text = condition.description.capitalizeFirst()
            pressureView.text = getString(R.string.pressure, weather.main?.pressure)
            windView.text = getString(R.string.wind_speed, weather.wind?.speed)
            humidityView.text = getString(R.string.humidity, weather.main?.humidity)
            realFeelView.text = getString(R.string.temp, weather.main?.feels_like ?: 0f)
            windDirectionView.text = weather.windDirection
            cloudinessView.text = getString(R.string.humidity, weather.clouds?.all)
            visibilityView.text = getString(R.string.humidity, weather.visibilityPercent)
        }
    }

    /**
     * Update the daily forecast card using the `DailyForecast` instance.
     */
    private fun refreshDailyForecast(forecast: DailyForecast) {
        val table = binding.dailyForecastTable
        table.removeAllViews()
        forecast.days.forEach { (date, day) ->
            Log.d(localClassName, "Creating a new daily forecast row ($date): $day")
            val dayRow = DayForecastRowBinding.inflate(layoutInflater)
            dayRow.dayView.text = day.getString() ?: getString(R.string.date_now)
            dayRow.dayConditionView.text = day.weatherDescription
            dayRow.dayTempView.text = getString(R.string.temp_min_max, day.minTemp, day.maxTemp)
            dayRow.dayWeatherImg.setImageResource(getWeatherIcon(day.weatherIcon))
            table.addView(dayRow.root)
        }
    }

    /**
     * Update the hourly forecast card using the `HourlyWeatherForecast` instance.
     */
    private fun refreshHourlyForecast(now: WeatherForecast, forecast: HourlyWeatherForecast) {
        val container = binding.hourlyForecastContainer
        container.removeAllViews()
        addHourCard(container, now)
        val currentTime = TimeUtils.localDateTime(System.currentTimeMillis() / 1000)
        var prevDay = currentTime.toLocalDate()
        forecast.list?.forEach { hourEntry ->
            val time = TimeUtils.utcDateTime(hourEntry.dt).toLocalZone()
            if (currentTime.isAfter(time))
                return@forEach

            val day = time.toLocalDate()
            if (day != prevDay) {
                val divider = HourlyForecastDividerBinding.inflate(layoutInflater, container, true)
                divider.dividerTextView.text =
                    day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            }
            addHourCard(container, hourEntry, time)
            prevDay = day
        }
    }

    /**
     * Add a single hourly weather data card into a `container` LinearLayout and populate it with
     * data from a WeatherForecast instance.
     */
    private fun addHourCard(
        container: LinearLayout,
        hourEntry: WeatherForecast,
        time: LocalDateTime? = null
    ) {
        val hourCard = HourlyForecastCardBinding.inflate(layoutInflater, container, true)
        hourCard.hourlyTempView.text = getString(R.string.temp_short, hourEntry.main?.temp)
        hourCard.weatherIcon.setImageResource(getWeatherIcon(hourEntry.mainWeatherCondition.icon))
        hourCard.hourlyDayView.text =
            if (time != null)
                TimeUtils.timeString(time.toLocalTime())
            else
                getString(R.string.time_now)
    }
}