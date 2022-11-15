package ua.edu.znu.hitonoriol.aweather

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ua.edu.znu.hitonoriol.aweather.databinding.ActivityWeatherBinding
import ua.edu.znu.hitonoriol.aweather.model.WeatherService
import ua.edu.znu.hitonoriol.aweather.model.data.HourlyWeatherForecast
import ua.edu.znu.hitonoriol.aweather.model.data.WeatherForecast

class WeatherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWeatherBinding
    private val weatherService = WeatherService("f8abc1bf1c752be95c7d49812de0f066")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))
        binding.toolbarLayout.title = title
        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        weatherService.fetchHourlyForecast(34.052235f, -118.243683f)
            .enqueue(object : Callback<HourlyWeatherForecast> {
                override fun onFailure(
                    call: Call<HourlyWeatherForecast>,
                    t: Throwable
                ) {
                    t.printStackTrace()
                }

                override fun onResponse(
                    call: Call<HourlyWeatherForecast>,
                    response: Response<HourlyWeatherForecast>
                ) {
                    println("API response: ${response.message()}")
                    var out: String = ""
                    for (entry in response.body()!!.list!!) {
                        out += "$entry "
                    }
                    binding.debugTextView.text = out
                }
            })
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
}