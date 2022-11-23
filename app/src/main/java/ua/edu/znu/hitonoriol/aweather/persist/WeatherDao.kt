package ua.edu.znu.hitonoriol.aweather.persist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO for the `LocalWeather` entity.
 */
@Dao
interface WeatherDao {
    /**
     * Retrieve a list of weather data for all cities ever selected
     * from the location selection activity.
     */
    @Query("SELECT * FROM weather")
    suspend fun getAll(): List<LocalWeather>

    /**
     * Retrieve previously saved weather data by `city` and `country`.
     * May return null if weather for the specified city was never fetched before.
     */
    @Query("SELECT * FROM weather WHERE " +
            "city LIKE :city AND " +
            "country LIKE :country LIMIT 1")
    suspend fun findByLocation(city: String, country: String): LocalWeather?

    /**
     * Persist a `LocalWeather` entity into the database, overwriting the existing one.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun persist(localWeather: LocalWeather)

    /**
     * Purge the entire weather data table.
     */
    @Query("DELETE FROM weather")
    suspend fun purge()
}