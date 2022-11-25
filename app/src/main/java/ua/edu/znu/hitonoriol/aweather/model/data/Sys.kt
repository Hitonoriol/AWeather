package ua.edu.znu.hitonoriol.aweather.model.data

/**
 * Represents the `sys` object of the `forecast5` and `current` API method responses.
 */
data class Sys(
    var pod: String? = null,
    var sunrise: Long = 0,
    var sunset: Long = 0
)
