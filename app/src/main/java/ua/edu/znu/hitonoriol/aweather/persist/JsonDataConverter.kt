package ua.edu.znu.hitonoriol.aweather.persist

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

open class JsonDataConverter<T> {
    companion object {
        val gson = Gson()
    }

    @TypeConverter
    fun fromObject(value: T): String {
        val type = object : TypeToken<T>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toObject(value: String): T {
        val type = object : TypeToken<T>() {}.type
        return gson.fromJson(value, type)
    }
}