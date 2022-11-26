package ua.edu.znu.hitonoriol.aweather.util

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import com.google.android.material.snackbar.Snackbar
import ua.edu.znu.hitonoriol.aweather.R

/**
 * Save a Double value as its raw bit pattern represented as a Long value to `SharedPreferences`.
 */
fun SharedPreferences.Editor.putDouble(key: String?, value: Double): SharedPreferences.Editor? {
    return putLong(key, value.toRawBits())
}

/**
 * Retrieve a Long value from `SharedPreferences` and treat it as a bit pattern of a Double value.
 */
fun SharedPreferences.getDouble(key: String?, defaultValue: Double): Double {
    return Double.fromBits(getLong(key, defaultValue.toRawBits()))
}

/**
 * Get this application's SharedPreferences.
 */
fun Context.getPrefs(): SharedPreferences {
    return getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE)
}

/**
 * Get a String value from `SharedPreferences` by a key stored as a string resource
 * with the specified id.
 */
fun Context.getStringPreference(keyStringId: Int): String? {
    return getPrefs().getString(getString(keyStringId), null)
}

/**
 * Get a Double value from `SharedPreferences` by a key stored as a string resource
 * with the specified id.
 */
fun Context.getDoublePreference(keyStringId: Int): Double {
    return getPrefs().getDouble(getString(keyStringId), .0)
}

fun Context.showSnackbar(view: View, message: String, duration: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(view, message, duration).show()
}

fun Context.showSnackbar(view: View, messageStringId: Int, length: Int = Snackbar.LENGTH_LONG) {
    showSnackbar(view, getString(messageStringId), length)
}