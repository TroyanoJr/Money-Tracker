package net.micode.spendingtracker.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sell
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Data model for a financial transaction stored in Room.
 * Linked to Category via ForeignKey with Cascade Delete.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["name"],
            childColumns = ["categoryName"],
            onDelete = ForeignKey.CASCADE // Este es el cambio clave
        )
    ],
    indices = [
        Index(value = ["categoryName"]),
        Index(value = ["date"])
    ] // Mejora rendimiento de búsqueda
)
data class Transaction(
    @PrimaryKey val id: String,
    val amount: Double,
    val categoryName: String,
    @Ignore val categoryIcon: ImageVector = Icons.Default.Sell,
    val date: Long,
    val note: String,
    val isExpense: Boolean,
    val isRepeating: Boolean = false,
    val isComplete: Boolean = true
) {
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
