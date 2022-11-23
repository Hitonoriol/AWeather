package ua.edu.znu.hitonoriol.aweather.persist

import android.content.Context
import androidx.room.*

/**
 * A database for persisting and restoring `LocalWeather` entities.
 * Used by `WeatherService`.
 */
@Database(entities = [LocalWeather::class], version = 1, exportSchema = false)
@TypeConverters(WeatherDataConverter::class)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao

    companion object {
        private var db: WeatherDatabase? = null

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