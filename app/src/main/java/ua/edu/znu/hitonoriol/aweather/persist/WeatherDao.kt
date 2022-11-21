package ua.edu.znu.hitonoriol.aweather.persist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather")
    fun getAll(): List<LocalWeather>

    @Query("SELECT * FROM weather WHERE " +
            "city LIKE :city AND " +
            "country LIKE :country LIMIT 1")
    fun findByLocation(city: String, country: String): LocalWeather

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun persist(localWeather: LocalWeather)
}