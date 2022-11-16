package ua.edu.znu.hitonoriol.aweather.model

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import ua.edu.znu.hitonoriol.aweather.model.data.HourlyWeatherForecast
import ua.edu.znu.hitonoriol.aweather.model.data.WeatherForecast

interface WeatherRequest {
    @GET("weather")
    fun fetchCurrent(@Query("appid") apiKey: String,
                     @Query("lat") latitude: Double,
                     @Query("lon") longitude: Double,
                     @Query("units") units: String = "metric"
    ): Call<WeatherForecast>

    @GET("forecast")
    fun fetchHourly(@Query("appid") apiKey: String,
                    @Query("lat") latitude: Double,
                    @Query("lon") longitude: Double,
                    @Query("units") units: String = "metric"
    ): Call<HourlyWeatherForecast>
}