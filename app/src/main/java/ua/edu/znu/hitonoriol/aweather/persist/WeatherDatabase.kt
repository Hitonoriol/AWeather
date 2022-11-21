package ua.edu.znu.hitonoriol.aweather.persist

import android.content.Context
import androidx.room.*
import ua.edu.znu.hitonoriol.aweather.model.data.DailyForecast
import ua.edu.znu.hitonoriol.aweather.model.data.HourlyWeatherForecast
import ua.edu.znu.hitonoriol.aweather.model.data.WeatherForecast

@Database(entities = [LocalWeather::class], version = 1, exportSchema = false)
@TypeConverters(
    WeatherForecastConverter::class,
    HourlyWeatherForecastConverter::class,
    DailyWeatherForecastConverter::class
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao

    companion object {
        var db: WeatherDatabase? = null
            private set

        fun initialize(context: Context): WeatherDatabase {
            if (db == null)
                db = Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabase::class.java, "weather-db"
                ).build()
            return db!!
        }
    }
}

class WeatherForecastConverter : JsonDataConverter<WeatherForecast>() {}
class HourlyWeatherForecastConverter : JsonDataConverter<HourlyWeatherForecast>() {}
class DailyWeatherForecastConverter : JsonDataConverter<DailyForecast>() {}