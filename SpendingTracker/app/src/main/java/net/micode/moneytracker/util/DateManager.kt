package net.micode.moneytracker.util

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
     * Calculates a [DateRange] based on the selected [Period] and a reference date,
     * taking into account custom start days for weeks and months.
     */
    fun calculateDateRange(
        p: Period, 
        d: Long, 
        monthStartDay: Int = 1, 
        weekStartDay: Int = Calendar.MONDAY
    ): DateRange {
        val s = Calendar.getInstance().apply {
            timeInMillis = d
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            when (p) {
                Period.WEEK -> {
                    // Adjust to the previous occurrence of weekStartDay
                    var diff = get(Calendar.DAY_OF_WEEK) - weekStartDay
                    if (diff < 0) diff += 7
                    add(Calendar.DAY_OF_YEAR, -diff)
                }
                Period.MONTH -> {
                    val currentDay = get(Calendar.DAY_OF_MONTH)
                    if (currentDay < monthStartDay) {
                        // We are in the period that started in the previous month
                        add(Calendar.MONTH, -1)
                    }
                    // Handle months with fewer days than monthStartDay
                    val maxDays = getActualMaximum(Calendar.DAY_OF_MONTH)
                    set(Calendar.DAY_OF_MONTH, monthStartDay.coerceAtMost(maxDays))
                }
                Period.YEAR -> {
                    // For YEAR, we usually stick to Jan 1st, but we could apply monthStartDay to Jan
                    set(Calendar.MONTH, Calendar.JANUARY)
                    set(Calendar.DAY_OF_MONTH, monthStartDay.coerceAtMost(getActualMaximum(Calendar.DAY_OF_MONTH)))
                }
                else -> Unit
            }
        }
        
        val e = s.clone() as Calendar
        when (p) {
            Period.DAY -> Unit
            Period.WEEK -> e.add(Calendar.DAY_OF_YEAR, 6)
            Period.MONTH -> {
                e.add(Calendar.MONTH, 1)
                e.add(Calendar.DAY_OF_YEAR, -1)
            }
            Period.YEAR -> {
                e.add(Calendar.YEAR, 1)
                e.add(Calendar.DAY_OF_YEAR, -1)
            }
        }
        e.set(Calendar.HOUR_OF_DAY, 23)
        e.set(Calendar.MINUTE, 59)
        e.set(Calendar.SECOND, 59)
        e.set(Calendar.MILLISECOND, 999)

        return DateRange(s.timeInMillis, e.timeInMillis)
    }
}
