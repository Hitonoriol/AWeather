package ua.edu.znu.hitonoriol.aweather.model.data

import androidx.room.Entity
import androidx.room.Ignore

data class WeatherForecast(
    var dt: Long = 0,
    var dt_txt: String? = null,
    var main: Main? = null,
    var clouds: Clouds? = null,
    var wind: Wind? = null,
    var visibility: Int? = null,
    var pop: Float? = null,
    var sys: Sys? = null
) {
    var mainWeatherCondition: Weather? = null

    @Ignore
    var weather: MutableList<Weather>? = null
        set(value) {
            field = value
            mainWeatherCondition = weather?.first()
        }
}
