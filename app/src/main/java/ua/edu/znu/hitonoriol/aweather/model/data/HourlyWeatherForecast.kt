package ua.edu.znu.hitonoriol.aweather.model.data

/**
 * Represents the root object of the `forecast5` (WeatherRequest#fetchHourly()) API method response.
 */
data class HourlyWeatherForecast(
    var list: MutableList<WeatherForecast>? = null
)
