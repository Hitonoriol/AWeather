package ua.edu.znu.hitonoriol.aweather.util

import android.content.SharedPreferences

fun SharedPreferences.saveString(key: String, value: String) {
    with(edit()) {
        putString(key, value)
        apply()
    }
}