package ua.edu.znu.hitonoriol.aweather.model

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import ua.edu.znu.hitonoriol.aweather.model.data.HourlyWeatherForecast
import ua.edu.znu.hitonoriol.aweather.model.data.WeatherForecast

/**
 * Bindings to Openweathermap API's methods.
 */
interface WeatherRequest {
    /**
     * Binding to the "current" API method (returns current weather data):
     *  https://openweathermap.org/current
     */
    @GET("weather")
    fun fetchCurrent(@Query("appid") apiKey: String,
                     @Query("lat") latitude: Double,
                     @Query("lon") longitude: Double,
                     @Query("units") units: String = "metric"
    ): Call<WeatherForecast>

    /**
     * Binding to the "forecast5" API method (returns 5-day hourly forecast with 3-hour step):
     *  https://openweathermap.org/forecast5
     */
    @GET("forecast")
    fun fetchHourly(@Query("appid") apiKey: String,
                    @Query("lat") latitude: Double,
                    @Query("lon") longitude: Double,
                    @Query("units") units: String = "metric"
    ): Call<HourlyWeatherForecast>
}