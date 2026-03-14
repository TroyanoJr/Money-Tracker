package net.micode.spendingtracker.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sell
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Data model for a financial transaction stored in Room.
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String,
    val amount: Double,
    val categoryName: String,
    @Ignore val categoryIcon: ImageVector = Icons.Default.Sell,
    val date: Long, // Timestamp
    val note: String,
    val isExpense: Boolean,
    val isRepeating: Boolean = false,
    val isComplete: Boolean = true // New field to track if the transaction needs more info
) {
    // Secondary constructor for Room to use, as it doesn't know how to handle ImageVector
    constructor(
        id: String,
        amount: Double,
        categoryName: String,
        date: Long,
        note: String,
        isExpense: Boolean,
        isRepeating: Boolean = false,
        isComplete: Boolean = true
    ) : this(id, amount, categoryName, Icons.Default.Sell, date, note, isExpense, isRepeating, isComplete)
}
