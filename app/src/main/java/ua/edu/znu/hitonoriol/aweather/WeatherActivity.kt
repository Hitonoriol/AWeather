package ua.edu.znu.hitonoriol.aweather

import android.Manifest
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
import androidx.core.util.Consumer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import ua.edu.znu.hitonoriol.aweather.databinding.ActivityWeatherBinding
import ua.edu.znu.hitonoriol.aweather.model.WeatherService
import ua.edu.znu.hitonoriol.aweather.util.capitalizeFirst
import ua.edu.znu.hitonoriol.aweather.util.execute

class WeatherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWeatherBinding
    private lateinit var weatherService: WeatherService
    private lateinit var locationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        weatherService = WeatherService("f8abc1bf1c752be95c7d49812de0f066", this)
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
                    updateCurrentWeather()
                }
            }
    }

    private fun updateCurrentWeather() {
        weatherService.fetchCurrentWeather()
            .execute(Consumer { weather ->
                val condition = weather.mainWeatherCondition!!
                binding.weatherConditionImg.setImageResource(
                    resources.getIdentifier("w${condition.icon?.dropLast(1)}","drawable", packageName))
                binding.temperatureView.setText("${weather.main?.temp} °C")
                binding.weatherConditionView.text = condition.description?.capitalizeFirst()
                setCityName(weatherService.cityName)
        })
    }
}