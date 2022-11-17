package ua.edu.znu.hitonoriol.aweather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isEmpty
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import ua.edu.znu.hitonoriol.aweather.databinding.ActivityWeatherBinding
import ua.edu.znu.hitonoriol.aweather.databinding.DayForecastRowBinding
import ua.edu.znu.hitonoriol.aweather.model.WeatherService
import ua.edu.znu.hitonoriol.aweather.model.data.DailyForecast
import ua.edu.znu.hitonoriol.aweather.model.data.WeatherForecast
import ua.edu.znu.hitonoriol.aweather.util.capitalizeFirst

class WeatherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWeatherBinding
    private lateinit var weatherService: WeatherService
    private lateinit var locationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        weatherService = WeatherService(resources.getString(R.string.owm_api_key), this)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))
        requestLocationPermissions()
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        setCityFromLocation()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_scrolling, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setCityName(name: String) {
        binding.cityNameView.text = name
        binding.toolbarLayout.title = name
    }

    private fun requestLocationPermissions() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (!permissions.containsKey(Manifest.permission.ACCESS_COARSE_LOCATION)
                && !permissions.containsKey(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(binding.root,
                    "This application requires location access to retrieve weather data",
                    Snackbar.LENGTH_LONG).show()
            }
        }

        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    private fun setCityFromLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
            override fun isCancellationRequested() = false
        })
            .addOnSuccessListener { location: Location? ->
                if (location == null)
                    Toast.makeText(this, "Cannot get location.", Toast.LENGTH_SHORT).show()
                else {
                    weatherService.setLocation(location.latitude, location.longitude)
                    refresh()
                }
            }
    }

    /**
     * Request an update from `weatherService` and call `updateCurrentWeather(weather)` if succeeded.
     */
    private fun refresh() {
        weatherService.getCurrentWeather { weather -> refreshCurrentWeather(weather) }
        weatherService.getDailyForecast { forecast -> refreshDailyForecast(forecast) }
    }

    /**
     * Update current weather info in collapsible title layout using the `weather` instance.
     */
    private fun refreshCurrentWeather(weather: WeatherForecast) {
        println("* Populating current weather layout with: $weather")
        val condition = weather.mainWeatherCondition!!
        setCityName(weatherService.cityName)
        binding.weatherConditionImg.setImageResource(getWeatherIcon(condition.icon!!))
        binding.temperatureView.setText("${weather.main?.temp} °C")
        binding.weatherConditionView.text = condition.description?.capitalizeFirst()
        /* Update bottom info bar */
        binding.pressureView.setText("${weather.main?.pressure} hPa")
        binding.windView.setText("${weather.wind?.speed} m/s")
        binding.humidityView.setText("${weather.main?.humidity}%")
    }

    private fun getWeatherIcon(name: String): Int {
        var iconName = "w${name.dropLast(1)}"
        println("* Icon: $iconName")
        return resources.getIdentifier(iconName,"drawable", packageName)
    }

    private fun refreshDailyForecast(forecast: DailyForecast) {
        val table = binding.dayForecastTable
        table.removeAllViews()
        forecast.days.forEach { (date, day) ->
            println("* Creating a new daily forecast row ($date): $day")
            val dayRow = DayForecastRowBinding.inflate(layoutInflater)
            dayRow.dayView.setText(day.getString())
            dayRow.dayConditionView.setText(day.weatherDescription)
            dayRow.dayTempView.setText("${day.minTemp}° / ${day.maxTemp}°")
            dayRow.dayWeatherImg.setImageResource(getWeatherIcon(day.weatherIcon))
            table.addView(dayRow.root)
        }
    }
}