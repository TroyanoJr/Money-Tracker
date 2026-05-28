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
 * Data model for a financial transaction stored in Room.
 * Linked to Category via ForeignKey with Cascade Delete.
 * Now also linked to Account via accountId.
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
    // Moved out of primary constructor to fix KSP matching error
    @Ignore
    var categoryIcon: ImageVector = Icons.Default.Sell

    // Constructor for UI/Logic that needs to pass an icon
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
