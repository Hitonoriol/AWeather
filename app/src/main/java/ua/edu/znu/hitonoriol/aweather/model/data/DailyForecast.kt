package ua.edu.znu.hitonoriol.aweather.model.data

import android.util.Log
import ua.edu.znu.hitonoriol.aweather.util.TimeUtils
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

/**
 * Encapsulates a map of forecasts for each unique day from an `HourlyWeatherForecast` object.
 */
class DailyForecast(hourlyForecast: HourlyWeatherForecast? = null) {
    var days: MutableMap<LocalDate, Day> = LinkedHashMap()

    init {
        if (hourlyForecast != null)
            generateForecast(hourlyForecast)
    }

    /**
     * Generate daily forecasts where:
     *  1. Minimum temperature for the day is the minimum of all hourly minimums for that day.
     *  2. Maximum temperature for the day is the maximum of all hourly maximums for that day.
     *  3. Weather condition for the day is the most frequently encountered weather condition
     *     of all hourly conditions for that day.
     */
    private fun generateForecast(hourlyForecast: HourlyWeatherForecast) {
        for(entry in hourlyForecast.list!!) {
            Log.i("","* Hourly forecast entry: $entry")
            val date = TimeUtils.utcDate(entry.dt)
            if (!days.containsKey(date))
                days[date] = Day(date)
            val day = days[date]!!

            day.weather[entry.mainWeatherCondition] =
                day.weather.getOrDefault(entry.mainWeatherCondition, 0) + 1

            if (entry.main!!.temp_min < day.minTemp)
                day.minTemp = entry.main!!.temp_min

            if (entry.main!!.temp_max > day.maxTemp)
                day.maxTemp = entry.main!!.temp_max
        }
    }

    /**
     * Represents a daily weather forecast.
     */
    data class Day(
        val date: LocalDate,
        val weather: MutableMap<Weather, Int> = HashMap(),
        var minTemp: Float = Float.MAX_VALUE,
        var maxTemp: Float = Float.MIN_VALUE
    ) {
        private var primaryWeatherCondition: Weather? = null
            get() {
                if (field == null)
                    field = weather.maxBy { it.value }.key
                return field
            }

        val weatherDescription: String
            get() = primaryWeatherCondition!!.description

        val weatherIcon: String
            get() = primaryWeatherCondition!!.icon

        fun getString() : String {
            if (TimeUtils.utcDate(System.currentTimeMillis() / 1000) == date)
                return "Today"

            return "${date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${date.dayOfMonth}"
        }
    }
}