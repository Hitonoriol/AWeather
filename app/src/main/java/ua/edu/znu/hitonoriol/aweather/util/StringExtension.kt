package ua.edu.znu.hitonoriol.aweather.util

import java.util.*

fun String.capitalizeFirst(): String {
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    return this
}