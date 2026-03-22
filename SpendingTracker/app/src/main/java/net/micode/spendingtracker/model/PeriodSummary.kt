package net.micode.spendingtracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

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
