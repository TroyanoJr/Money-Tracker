package net.micode.spendingtracker.util

import java.util.*

/**
 * Supported time periods for transaction filtering and reporting.
 */
enum class Period { DAY, WEEK, MONTH, YEAR }

/**
 * Internal helper to manage start and end timestamps for a given period.
 */
data class DateRange(val start: Long, val end: Long)

/**
 * Utility class to handle date calculations and range generations.
 */
object DateManager {

    /**
     * Adjusts a date by adding or subtracting units of a given [Period].
     */
    fun adjustDate(d: Long, p: Period, a: Int): Long = Calendar.getInstance().apply {
        timeInMillis = d
        when (p) {
            Period.DAY -> add(Calendar.DAY_OF_YEAR, a)
            Period.WEEK -> add(Calendar.WEEK_OF_YEAR, a)
            Period.MONTH -> add(Calendar.MONTH, a)
            Period.YEAR -> add(Calendar.YEAR, a)
        }
    }.timeInMillis

    /**
     * Calculates a [DateRange] based on the selected [Period] and a reference date.
     */
    fun calculateDateRange(p: Period, d: Long): DateRange {
        val s = Calendar.getInstance().apply {
            timeInMillis = d
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            when (p) {
                Period.WEEK -> set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                Period.MONTH -> set(Calendar.DAY_OF_MONTH, 1)
                Period.YEAR -> {
                    set(Calendar.MONTH, Calendar.JANUARY)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                else -> Unit
            }
        }
        val e = s.clone() as Calendar
        when (p) {
            Period.DAY -> Unit
            Period.WEEK -> e.add(Calendar.DAY_OF_YEAR, 6)
            Period.MONTH -> e.set(Calendar.DAY_OF_MONTH, e.getActualMaximum(Calendar.DAY_OF_MONTH))
            Period.YEAR -> {
                e.set(Calendar.MONTH, Calendar.DECEMBER)
                e.set(Calendar.DAY_OF_MONTH, 31)
            }
        }
        e.set(Calendar.HOUR_OF_DAY, 23)
        e.set(Calendar.MINUTE, 59)
        e.set(Calendar.SECOND, 59)
        e.set(Calendar.MILLISECOND, 999)
        return DateRange(s.timeInMillis, e.timeInMillis)
    }
}
