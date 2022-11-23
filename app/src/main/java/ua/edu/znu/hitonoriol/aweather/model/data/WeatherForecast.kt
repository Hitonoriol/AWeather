package ua.edu.znu.hitonoriol.aweather.model.data

/**
 * Represents the root object of the `current` (WeatherRequest#fetchCurrent()) API method response.
 */
data class WeatherForecast(
    var dt: Long = 0,
    var dt_txt: String? = null,
    var main: Main? = null,
    var weather: MutableList<Weather>? = null,
    var clouds: Clouds? = null,
    var wind: Wind? = null,
    var visibility: Int? = null,
    var pop: Float? = null,
    var sys: Sys? = null
) {
    val mainWeatherCondition: Weather
        get() = weather?.first()!!
}
