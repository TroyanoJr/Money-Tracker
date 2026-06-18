package net.micode.spendingtracker.util

import net.micode.spendingtracker.model.Account
import net.micode.spendingtracker.repository.TransactionRepository
import java.util.Calendar

/**
 * Utility to handle budget calculations and carry-over logic.
 * This separates business rules from the ViewModel, adhering to SRP.
 */
object BudgetManager {

    /**
     * Calculates the amount to be carried over from previous periods.
     */
    suspend fun calculateCarryOver(
        accountId: Long,
        rangeStart: Long,
        settingsManager: SettingsManager,
        repository: TransactionRepository,
        allAccounts: List<Account>
    ): Double {
        if (accountId == -1L) {
            // Consolidated carry for All Accounts (Budget must be OFF for this view)
            if (settingsManager.isBudgetModeEnabled(-1L)) return 0.0
            
            var totalCarry = 0.0
            for (acc in allAccounts) {
                if (settingsManager.isCarryOverEnabled(acc.id) && !settingsManager.isBudgetModeEnabled(acc.id)) {
                    val accCarry = repository.getBalanceBeforeDate(acc.id, rangeStart)
                    val accPosOnly = settingsManager.isCarryOverPositiveOnly(acc.id)
                    totalCarry += if (accPosOnly && accCarry < 0.0) 0.0 else accCarry
                }
            }
            return totalCarry
        }
        
        if (!settingsManager.isCarryOverEnabled(accountId)) return 0.0
        
        val posOnly = settingsManager.isCarryOverPositiveOnly(accountId)
        
        return if (settingsManager.isBudgetModeEnabled(accountId)) {
            val budget = settingsManager.getMonthlyBudget(accountId)
            val includeInc = settingsManager.isIncludeIncomeInBudget(accountId)
            calculateBudgetCarryOver(accountId, rangeStart, budget, includeInc, posOnly, repository, settingsManager)
        } else {
            val balanceBefore = repository.getBalanceBeforeDate(accountId, rangeStart)
            if (posOnly && balanceBefore < 0.0) 0.0 else balanceBefore
        }
    }

    private suspend fun calculateBudgetCarryOver(
        accId: Long,
        startDate: Long,
        budget: Double,
        includeInc: Boolean,
        posOnly: Boolean,
        repository: TransactionRepository,
        settingsManager: SettingsManager
    ): Double {
        val periodStr = settingsManager.getDefaultTimePeriod()
        val period = try { Period.valueOf(periodStr) } catch (e: Exception) { Period.MONTH }
        val monthStartDay = settingsManager.getMonthStartDay()
        val weekStartDay = settingsManager.getWeekStartDay()

        val firstTxDate = repository.getOldestTransactionDate(accId) ?: return 0.0
        
        // Calculate the exact start of the FIRST period in history
        val startOfFirstPeriod = DateManager.calculateDateRange(
            period, firstTxDate, monthStartDay, weekStartDay
        ).start
        
        if (startDate <= startOfFirstPeriod) return 0.0
        
        // Calculate how many periods have passed
        val periodsPassed = when (period) {
            Period.DAY -> {
                ((startDate - startOfFirstPeriod) / (24 * 60 * 60 * 1000)).toInt()
            }
            Period.WEEK -> {
                ((startDate - startOfFirstPeriod) / (7 * 24 * 60 * 60 * 1000)).toInt()
            }
            Period.MONTH -> {
                val calStart = Calendar.getInstance().apply { timeInMillis = startOfFirstPeriod }
                val calCurrent = Calendar.getInstance().apply { timeInMillis = startDate }
                val years = calCurrent.get(Calendar.YEAR) - calStart.get(Calendar.YEAR)
                val months = calCurrent.get(Calendar.MONTH) - calStart.get(Calendar.MONTH)
                (years * 12) + months
            }
            Period.YEAR -> {
                val calStart = Calendar.getInstance().apply { timeInMillis = startOfFirstPeriod }
                val calCurrent = Calendar.getInstance().apply { timeInMillis = startDate }
                calCurrent.get(Calendar.YEAR) - calStart.get(Calendar.YEAR)
            }
        }
        
        if (periodsPassed <= 0) return 0.0
        
        val expensesBefore = repository.getTotalExpensesBeforeDate(accId, startDate)
        val cumulativeBudget = budget * periodsPassed
        val incomeBefore = if (includeInc) repository.getTotalIncomeBeforeDate(accId, startDate) else 0.0
        
        val carry = (cumulativeBudget + incomeBefore) - expensesBefore
        return if (posOnly && carry < 0.0) 0.0 else carry
    }
}
