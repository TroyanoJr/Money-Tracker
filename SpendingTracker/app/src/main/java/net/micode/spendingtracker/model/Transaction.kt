package net.micode.spendingtracker.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sell
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Data model for a financial transaction stored in the Room database.
 * Each transaction is linked to a [Category] and an [Account].
 * 
 * @property id Unique identifier for the transaction (often a UUID string).
 * @property amount The monetary value of the transaction.
 * @property categoryName The name of the category this transaction belongs to (Foreign Key).
 * @property date The timestamp when the transaction occurred.
 * @property note A user-provided description or note for the transaction.
 * @property isExpense True if the transaction is an expense, false if it is income.
 * @property isRepeating Whether this is a recurring transaction.
 * @property isComplete Whether the transaction has been finalized.
 * @property accountId The ID of the account associated with this transaction (Foreign Key).
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["name"],
            childColumns = ["categoryName"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["categoryName"]),
        Index(value = ["date"]),
        Index(value = ["accountId"])
    ]
)
data class Transaction(
    @PrimaryKey val id: String,
    val amount: Double,
    val categoryName: String,
    val date: Long,
    val note: String,
    val isExpense: Boolean,
    val isRepeating: Boolean = false,
    val isComplete: Boolean = true,
    @ColumnInfo(name = "accountId") val accountId: Long = 0
) {
    /**
     * The icon vector representing the transaction's category.
     * Ignored by Room as it's only used for UI display.
     */
    @Ignore
    var categoryIcon: ImageVector = Icons.Default.Sell

    /**
     * Secondary constructor for UI/Logic that needs to pass an icon explicitly.
     */
    @Ignore
    constructor(
        id: String,
        amount: Double,
        categoryName: String,
        categoryIcon: ImageVector,
        date: Long,
        note: String,
        isExpense: Boolean,
        isRepeating: Boolean = false,
        isComplete: Boolean = true,
        accountId: Long = 0
    ) : this(id, amount, categoryName, date, note, isExpense, isRepeating, isComplete, accountId) {
        this.categoryIcon = categoryIcon
    }
}
