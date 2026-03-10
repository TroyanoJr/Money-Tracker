package net.micode.spendingtracker.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data model for a financial transaction.
 */
data class Transaction(
    val id: String,
    val amount: Double,
    val categoryName: String,
    val categoryIcon: ImageVector,
    val date: Long, // Timestamp
    val note: String,
    val isExpense: Boolean,
    val isRepeating: Boolean = false
)
