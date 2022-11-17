package ua.edu.znu.hitonoriol.aweather.model.data

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.TextStyle
import java.util.Locale

class DailyForecast(private val hourlyForecast: HourlyWeatherForecast) {
    val days: MutableMap<LocalDate, Day> = LinkedHashMap()

    init {
        for(entry in hourlyForecast.list!!) {
            println("* Hourly forecast entry: $entry")
            val date = utcDate(entry.dt)
            if (!days.containsKey(date))
                days[date] = Day(date)
            val day = days[date]!!

            day.weather[entry.mainWeatherCondition!!] =
                day.weather.getOrDefault(entry.mainWeatherCondition, 0) + 1

            if (entry.main!!.temp_min < day.minTemp)
                day.minTemp = entry.main!!.temp_min

            if (entry.main!!.temp_max > day.maxTemp)
                day.maxTemp = entry.main!!.temp_max
        }
    }

    private fun utcDate(seconds: Long) : LocalDate {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneOffset.UTC).toLocalDate()
    }

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
            get() = primaryWeatherCondition!!.description!!

        val weatherIcon: String
            get() = primaryWeatherCondition!!.icon!!

        fun getString() : String {
            return "${date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${date.dayOfMonth}"
        }
    }
}