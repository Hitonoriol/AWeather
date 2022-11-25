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
    var visibility: Int = 0,
    var pop: Float? = null,
    var sys: Sys? = null
) {
    val mainWeatherCondition: Weather
        get() = weather?.first()!!

    val visibilityPercent: Int
        get() = (100f * (visibility.toFloat() / maxVisibility)).toInt()

    val windDirection: String
        get() {
            return when (wind?.deg) {
                in 0..39 -> "N"
                in 40..79 -> "NE"
                in 80..129 -> "E"
                in 130..169 -> "SE"
                in 170..219 -> "S"
                in 220..259 -> "SW"
                in 260..309 -> "W"
                in 310..349 -> "NW"
                in 350..360 -> "N"
                else -> ""
            }
        }

    companion object {
        private const val maxVisibility = 10000
    }
}
