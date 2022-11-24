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
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import ua.edu.znu.hitonoriol.aweather.databinding.ActivityLocationSelectionBinding
import ua.edu.znu.hitonoriol.aweather.util.getPrefs
import ua.edu.znu.hitonoriol.aweather.util.getStringPreference
import ua.edu.znu.hitonoriol.aweather.util.putDouble
import java.util.*

/**
 * Activity for selecting a city to retrieve weather data for.
 */
class LocationSelectionActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityLocationSelectionBinding
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    private lateinit var locationFragment: AutocompleteSupportFragment

    private lateinit var mapFragment: SupportMapFragment
    private var gMap: GoogleMap? = null
    companion object {
        private const val maxZoom = 11f
        private const val cityZoom = 10f
    }

    private var coordinates: LatLng? = null
    private var city: String = ""
    private var country: String = ""
    private val locationString
        get() = "$city, $country"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestLocationPermissions()
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        initLocationAutocomplete()
        restoreSavedLocation()

        binding.currentLocationBtn.setOnClickListener {
            getCurrentLocation { coordinates ->
                if (gMap != null)
                    panMapTo(coordinates)
                else
                    saveLocation(coordinates)
            }
        }

        binding.applyLocationBtn.setOnClickListener {
            applyLocation()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        gMap = map
        val settings = gMap!!.uiSettings
        settings.isMyLocationButtonEnabled = false
        settings.isMapToolbarEnabled = false
        settings.isIndoorLevelPickerEnabled = false
        gMap?.setMaxZoomPreference(maxZoom)
        gMap?.setOnMapClickListener { panMapTo(it) }
    }

    /**
     * Fill Places autocomplete text field with the previously selected city (if any).
     */
    private fun restoreSavedLocation() {
        val city = getStringPreference(R.string.pref_city)
        val country = getStringPreference(R.string.pref_country)
        if (city == null || country == null)
            return

        this.city = city
        this.country = country
        locationFragment.setText(locationString)
    }

    /**
     * Save the specified `latitude` and `longitude` with country and city name associated with
     * the specified coordinates to SharedPreferences.
     */
    private fun saveLocation(coordinates: LatLng, onSuccess: () -> Unit = {}) {
        enableButtons(false)
        lifecycleScope.launch {
            val locationList =
                geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1)
            if (locationList == null || locationList.isEmpty()) {
                runOnUiThread {
                    Snackbar.make(
                        binding.root,
                        "Cannot retrieve your current city / country name.",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                return@launch
            }

            this@LocationSelectionActivity.coordinates = coordinates
            val location = locationList.first()
            country = location.countryName
            city =
                if (location.locality != null)
                    location.locality
                else if (location.subLocality != null)
                    location.subLocality
                else if (location.subAdminArea != null)
                    location.subAdminArea
                else if (location.adminArea != null)
                    location.adminArea
                else
                    ""
            runOnUiThread {
                locationFragment.setText(locationString)
                enableButtons(true)
                onSuccess()
            }
        }
    }

    private fun enableButtons(enable: Boolean) {
        binding.applyLocationBtn.isEnabled = enable
        binding.currentLocationBtn.isEnabled = enable
    }

    private fun applyLocation() {
        if (city.isEmpty() || country.isEmpty() || coordinates == null) {
            Snackbar.make(binding.root, "No location is selected!", Snackbar.LENGTH_LONG).show()
            return
        }

        with(getPrefs().edit()) {
            putString(getString(R.string.pref_city), city)
            putString(getString(R.string.pref_country), country)
            putDouble(getString(R.string.pref_lat), coordinates!!.latitude)
            putDouble(getString(R.string.pref_lon), coordinates!!.longitude)
            apply()
        }
        finish()
    }

    private fun requestLocationPermissions() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (!permissions.containsKey(Manifest.permission.ACCESS_COARSE_LOCATION)
                && !permissions.containsKey(Manifest.permission.ACCESS_FINE_LOCATION)
            ) {
                Snackbar.make(
                    binding.root,
                    "This application requires location access to retrieve weather data",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    /**
     * Get current longitude and latitude from the android location API and pass it to
     * the `action` consumer.
     */
    private fun getCurrentLocation(action: (LatLng) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
        }
        enableButtons(false)
        Snackbar.make(binding.root, "Determining your current location...", Snackbar.LENGTH_LONG)
            .show()
        locationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                    CancellationTokenSource().token

                override fun isCancellationRequested() = false
            })
            .addOnSuccessListener { location: Location? ->
                runOnUiThread { enableButtons(true) }
                if (location == null)
                    Snackbar.make(
                        binding.root,
                        "Cannot get current location.",
                        Snackbar.LENGTH_LONG
                    ).show()
                else
                    action(LatLng(location.latitude, location.longitude))
            }
    }

    /**
     * Initialize Google Places API autocomplete fragment.
     */
    private fun initLocationAutocomplete() {
        if (!Places.isInitialized())
            Places.initialize(
                applicationContext,
                getString(R.string.google_maps_api_key),
                Locale.getDefault()
            )

        locationFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                as AutocompleteSupportFragment
        locationFragment.setHint(getString(R.string.city_field_hint))
        locationFragment.setPlaceFields(listOf(Place.Field.LAT_LNG))
        locationFragment.setTypeFilter(TypeFilter.CITIES)
        locationFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val latLng = place.latLng ?: return
                Log.i(TAG, "Place: ${place.latLng}")
                if (gMap == null)
                    saveLocation(latLng)
                else
                    panMapTo(latLng)
            }

            override fun onError(status: Status) {
                Log.i(TAG, "Places autocomplete error: $status")
            }
        })
    }

    private fun panMapTo(coordinates: LatLng) {
        if (gMap == null)
            return

        gMap?.clear()
        val marker = gMap?.addMarker(MarkerOptions().position(coordinates))
        gMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, cityZoom))
        saveLocation(coordinates) {
            marker?.title = locationString
            marker?.showInfoWindow()
        }
    }
}