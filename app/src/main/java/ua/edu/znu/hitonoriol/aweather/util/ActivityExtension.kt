package ua.edu.znu.hitonoriol.aweather.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import ua.edu.znu.hitonoriol.aweather.R

fun SharedPreferences.Editor.putDouble(key: String?, value: Double): SharedPreferences.Editor? {
    return putLong(key, value.toRawBits())
}

fun SharedPreferences.getDouble(key: String?, defaultValue: Double): Double {
    return Double.fromBits(getLong(key, defaultValue.toRawBits()))
}

fun Context.getPrefs(): SharedPreferences {
    return getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE)
}

fun Context.getStringPreference(keyStringId: Int): String? {
    return getPrefs().getString(getString(keyStringId), null)
}

fun Context.getDoublePreference(keyStringId: Int): Double {
    return getPrefs().getDouble(getString(keyStringId), .0)
}