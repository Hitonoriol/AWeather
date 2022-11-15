package ua.edu.znu.hitonoriol.aweather.model.data

data class WeatherForecast(
    var dt: Long? = null,
    var dt_txt: String? = null,
    var main: Main? = null,
    var weather: MutableList<Weather>? = null,
    var clouds: Clouds? = null,
    var wind: Wind? = null,
    var visibility: Int? = null,
    var pop: Float? = null,
    var sys: Sys? = null
)
