package ua.edu.znu.hitonoriol.aweather.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

/**
 * Time formatting and conversion utils.
 */
class TimeUtils {
    companion object {
        val locale = Locale.UK
        private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(locale)

        fun localDateTime(
            seconds: Long,
            timeZone: ZoneId = ZoneId.systemDefault()
        ): LocalDateTime {
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), timeZone)
        }

        fun utcDateTime(seconds: Long): LocalDateTime {
            return localDateTime(seconds, ZoneOffset.UTC)
        }

        fun timeString(time: LocalTime): String {
            return formatter.format(time)
        }

        fun dateString(date: LocalDate): String {
            return dateFormatter.format(date)
        }
    }
}

fun LocalDateTime.toLocalZone(): LocalDateTime {
    return atZone(ZoneId.systemDefault()).toLocalDateTime()
}