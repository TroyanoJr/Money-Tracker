package net.micode.spendingtracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a user profile or account wallet.
 */
@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int,
    val isDefault: Boolean = false
)
