package ua.edu.znu.hitonoriol.aweather.model.data

/**
 * Represents the `wind` object of the `forecast5` and `current` API method responses.
 */
data class Wind(
    var speed: Float = 0f,
    var deg: Int = 0,
    var gust: Float = 0f
)
