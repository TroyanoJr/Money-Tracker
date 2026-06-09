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
            calculateBudgetCarryOver(accountId, rangeStart, budget, includeInc, posOnly, repository)
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
        repository: TransactionRepository
    ): Double {
        val firstTxDate = repository.getOldestTransactionDate(accId) ?: return 0.0
        
        val calStart = Calendar.getInstance().apply { 
            timeInMillis = firstTxDate
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calCurrent = Calendar.getInstance().apply { 
            timeInMillis = startDate
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val monthsDiff = (calCurrent.get(Calendar.YEAR) - calStart.get(Calendar.YEAR)) * 12 + 
                        (calCurrent.get(Calendar.MONTH) - calStart.get(Calendar.MONTH))
        
        if (monthsDiff <= 0) return 0.0
        
        val expensesBefore = repository.getTotalExpensesBeforeDate(accId, startDate)
        val cumulativeBudget = budget * monthsDiff
        val incomeBefore = if (includeInc) repository.getTotalIncomeBeforeDate(accId, startDate) else 0.0
        
        val carry = (cumulativeBudget + incomeBefore) - expensesBefore
        return if (posOnly && carry < 0.0) 0.0 else carry
    }
}
