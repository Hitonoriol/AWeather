package ua.edu.znu.hitonoriol.aweather.model.data

import androidx.room.Entity

data class HourlyWeatherForecast(
    var list: MutableList<WeatherForecast>? = null
)
