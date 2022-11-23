package ua.edu.znu.hitonoriol.aweather.model.data

/**
 * Represents the `main` object of the `forecast5` and `current` API method responses.
 */
data class Main(
    var temp: Float = 0f,
    var feels_like: Float = 0f,
    var temp_min: Float = 0f,
    var temp_max: Float = 0f,
    var pressure: Int = 0,
    var sea_level: Int = 0,
    var grnd_level: Int = 0,
    var humidity: Int = 0,
    var temp_kf: Float = 0f
)
