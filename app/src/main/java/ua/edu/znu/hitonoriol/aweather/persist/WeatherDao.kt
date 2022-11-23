package ua.edu.znu.hitonoriol.aweather.persist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather")
    suspend fun getAll(): List<LocalWeather>

    @Query("SELECT * FROM weather WHERE " +
            "city LIKE :city AND " +
            "country LIKE :country LIMIT 1")
    suspend fun findByLocation(city: String, country: String): LocalWeather?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun persist(localWeather: LocalWeather)

    @Query("DELETE FROM weather")
    suspend fun purge()
}