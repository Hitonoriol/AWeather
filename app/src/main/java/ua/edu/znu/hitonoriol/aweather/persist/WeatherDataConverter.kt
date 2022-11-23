package ua.edu.znu.hitonoriol.aweather.persist

import androidx.room.TypeConverter
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import ua.edu.znu.hitonoriol.aweather.model.data.DailyForecast
import ua.edu.znu.hitonoriol.aweather.model.data.HourlyWeatherForecast
import ua.edu.znu.hitonoriol.aweather.model.data.WeatherForecast
import java.lang.reflect.Type
import java.time.LocalDate

open class WeatherDataConverter {
    companion object {
        private val gson = GsonBuilder()
            .enableComplexMapKeySerialization()
            .create()

        private val currentType = object : TypeToken<WeatherForecast>() {}.type!!
        private val hourlyType = object : TypeToken<HourlyWeatherForecast>() {}.type!!
        private val dailyType = object : TypeToken<DailyForecast>() {}.type!!
    }

    @TypeConverter fun fromCurrent(value: WeatherForecast): String = gson.toJson(value, currentType)
    @TypeConverter fun toCurrent(value: String): WeatherForecast = gson.fromJson(value, currentType)

    @TypeConverter fun fromHourly(value: HourlyWeatherForecast): String = gson.toJson(value, hourlyType)
    @TypeConverter fun toHourly(value: String): HourlyWeatherForecast = gson.fromJson(value, hourlyType)

    @TypeConverter fun fromDaily(value: DailyForecast): String = gson.toJson(value, dailyType)
    @TypeConverter fun toDaily(value: String): DailyForecast = gson.fromJson(value, dailyType)
}