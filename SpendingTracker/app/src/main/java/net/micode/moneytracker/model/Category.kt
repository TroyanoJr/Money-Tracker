package net.micode.moneytracker.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a financial category for grouping transactions.
 * Categories can be for either expenses or income and have an associated icon and color.
 * 
 * @property id Unique identifier for the category.
 * @property name The display name of the category (must be unique).
 * @property iconName The name of the icon to be displayed for this category.
 * @property isExpense True if this category is for expenses, false for income.
 * @property color The color code used to represent this category in charts and UI.
 */
@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)] // Required for Foreign Key constraints
)
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String,
    val isExpense: Boolean,
    val color: Int? = null
)
