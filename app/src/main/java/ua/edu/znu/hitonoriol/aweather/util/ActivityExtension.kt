package ua.edu.znu.hitonoriol.aweather.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import ua.edu.znu.hitonoriol.aweather.R

fun Activity.getPrefs(): SharedPreferences {
    return getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE)
}