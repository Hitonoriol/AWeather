package ua.edu.znu.hitonoriol.aweather.model.data

data class Main(
    var temp: Float? = null,
    var feels_like: Float? = null,
    var temp_min: Float? = null,
    var temp_max: Float? = null,
    var pressure: Int? = null,
    var sea_level: Int? = null,
    var grnd_level: Int? = null,
    var humidity: Int? = null,
    var temp_kf: Float? = null
)
