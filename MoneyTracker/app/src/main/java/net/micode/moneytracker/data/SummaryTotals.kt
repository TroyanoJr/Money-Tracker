package net.micode.moneytracker.data

/**
 * A simple data class used to hold aggregated financial totals.
 * 
 * @property totalIncome The sum of all income amounts in a given context.
 * @property totalExpense The sum of all expense amounts in a given context.
 */
data class SummaryTotals(
    val totalIncome: Double,
    val totalExpense: Double
)
