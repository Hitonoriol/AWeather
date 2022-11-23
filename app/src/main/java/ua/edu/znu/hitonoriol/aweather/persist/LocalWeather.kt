package ua.edu.znu.hitonoriol.aweather.persist

import androidx.room.Entity
import ua.edu.znu.hitonoriol.aweather.model.data.DailyForecast
import ua.edu.znu.hitonoriol.aweather.model.data.HourlyWeatherForecast
import ua.edu.znu.hitonoriol.aweather.model.data.WeatherForecast
/**
 * Represents the latest weather data for the location specified by `city` and `country` names.
 * Only one instance is stored in the database for each unique location.
 */
@Entity(tableName = "weather", primaryKeys = ["city", "country"])
data class LocalWeather(
    var city: String = "",
    var country: String = "",
    var latitude: Double = .0,
    var longitude: Double = .0,

    var lastUpdate: Long = 0,

    var hourlyForecast: HourlyWeatherForecast? = null,
    var currentForecast: WeatherForecast? = null,
    var dailyForecast: DailyForecast? = null
) {
    val valid: Boolean
        get() = currentForecast != null && hourlyForecast != null && dailyForecast != null

    fun invalidate() {
        currentForecast = null
        hourlyForecast = null
        dailyForecast = null
    }

    fun setLocation(city: String, country: String, latitude: Double, longitude: Double) {
        this.city = city
        this.country = country
        this.latitude = latitude
        this.longitude = longitude
    }
}