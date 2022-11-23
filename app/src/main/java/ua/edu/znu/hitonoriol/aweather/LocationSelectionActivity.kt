package ua.edu.znu.hitonoriol.aweather

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager.TAG
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.snackbar.Snackbar
import ua.edu.znu.hitonoriol.aweather.databinding.ActivityLocationSelectionBinding
import ua.edu.znu.hitonoriol.aweather.util.getPrefs
import ua.edu.znu.hitonoriol.aweather.util.getStringPreference
import ua.edu.znu.hitonoriol.aweather.util.putDouble
import java.util.*


class LocationSelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLocationSelectionBinding
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationFragment: AutocompleteSupportFragment
    private lateinit var geocoder: Geocoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        requestLocationPermissions()
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this)
        initLocationAutocomplete()
        restoreSavedLocation()

        binding.fab.setOnClickListener {
            getCurrentLocation { lat, lon ->
                locationFragment.setText("$lat, $lon")
            }
        }
    }

    private fun restoreSavedLocation() {
        val city = getStringPreference(R.string.pref_city)
        val country = getStringPreference(R.string.pref_country)
        if (city == null || country == null)
            return

        locationFragment.setText("$city, $country")
    }

    private fun saveLocation(latitude: Double, longitude: Double) {
        val locationList = geocoder.getFromLocation(latitude, longitude, 1)
        if (locationList == null || locationList.isEmpty()) {
            Snackbar.make(binding.root,
                "Cannot retrieve your current city / country name.",
                Snackbar.LENGTH_LONG).show()
            return
        }

        val location = locationList.first()
        with(getPrefs().edit()) {
            putString(getString(R.string.pref_city), location.locality)
            putString(getString(R.string.pref_country), location.countryName)
            putDouble(getString(R.string.pref_lat), latitude)
            putDouble(getString(R.string.pref_lon), longitude)
            apply()
        }
        finish()
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

    private fun getCurrentLocation(action: (Double, Double) -> Unit) {
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
                    Snackbar.make(binding.root, "Cannot get current location.", Snackbar.LENGTH_LONG).show()
                else
                    action(location.latitude, location.longitude)
            }
    }

    private fun initLocationAutocomplete() {
        if (!Places.isInitialized())
            Places.initialize(applicationContext, getString(R.string.google_maps_api_key), Locale.getDefault())

        locationFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                as AutocompleteSupportFragment
        locationFragment.setHint(getString(R.string.city_field_hint))
        locationFragment.setPlaceFields(listOf(Place.Field.LAT_LNG))
        locationFragment.setTypeFilter(TypeFilter.CITIES)
        locationFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val latLng = place.latLng
                if (latLng != null) {
                    Log.i(TAG, "Place: ${place.latLng}")
                    saveLocation(latLng.latitude, latLng.longitude)
                }
            }

            override fun onError(status: Status) {
                Log.i(TAG, "Places autocomplete error: $status")
            }
        })
    }
}