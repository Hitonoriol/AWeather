package ua.edu.znu.hitonoriol.aweather.util

import java.time.*
import java.time.format.DateTimeFormatter

/**
 * Time formatting and conversion utils.
 */
class TimeUtils {
    companion object {
        private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        fun localDateTime(
            seconds: Long,
            timeZone: ZoneId = ZoneId.systemDefault()
        ): LocalDateTime {
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), timeZone)
        }

        fun localDate(seconds: Long, timeZone: ZoneId = ZoneId.systemDefault()): LocalDate {
            return localDateTime(seconds, timeZone).toLocalDate()
        }

        fun utcDateTime(seconds: Long): LocalDateTime {
            return localDateTime(seconds, ZoneOffset.UTC)
        }

        fun utcDate(seconds: Long): LocalDate {
            return localDate(seconds, ZoneOffset.UTC)
        }

        fun timeString(time: LocalTime): String {
            return formatter.format(time)
        }
    }
}