package net.micode.spendingtracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data model representing a cached financial summary for a specific time period and filter.
 * This is used to speed up report generation by storing pre-calculated totals.
 * 
 * @property summaryKey A unique identifier generated based on period, date range, and filters.
 * @property period The type of period (e.g., "MONTH", "YEAR").
 * @property startDate The starting timestamp of the period.
 * @property endDate The ending timestamp of the period.
 * @property filterType The filter applied (e.g., "ALL", "ONLY_EXPENSE").
 * @property categoryName The name of the category filter, if applicable.
 * @property totalIncome The calculated sum of income in this period.
 * @property totalExpense The calculated sum of expenses in this period.
 * @property balance The net balance (Income - Expense).
 * @property updatedAt The timestamp when this summary was last calculated.
 */
@Entity(tableName = "period_summaries")
data class PeriodSummary(
    @PrimaryKey val summaryKey: String,
    val period: String,
    val startDate: Long,
    val endDate: Long,
    val filterType: String,
    val categoryName: String?,
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val updatedAt: Long
)
