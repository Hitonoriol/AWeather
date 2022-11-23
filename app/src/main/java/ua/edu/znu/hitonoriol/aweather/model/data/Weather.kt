package ua.edu.znu.hitonoriol.aweather.model.data

/**
 * Represents an element of the `weather` array of the `forecast5` and `current`
 * API method responses.
 */
data class Weather(
    var id: Int = 0,
    var main: String = "",
    var description: String = "",
    var icon: String = ""
)
