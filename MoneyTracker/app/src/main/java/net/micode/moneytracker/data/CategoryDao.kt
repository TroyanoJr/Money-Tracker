package net.micode.moneytracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.micode.moneytracker.model.Category

/**
 * Data Access Object for the [Category] entity.
 * Provides methods for managing financial categories.
 */
@Dao
interface CategoryDao {
    /**
     * Retrieves all categories from the database.
     * @return A Flow of a list containing all categories.
     */
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    /**
     * Inserts a new category into the database. If it exists, it replaces it.
     * @param category The category to insert.
     * @return The row ID of the newly inserted category.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    /**
     * Deletes a specific category from the database.
     * @param category The category to delete.
     * @return The number of rows affected.
     */
    @Delete
    suspend fun deleteCategory(category: Category): Int

    /**
     * Updates an existing category in the database.
     * @param category The category with updated information.
     * @return The number of rows affected.
     */
    @Update
    suspend fun updateCategory(category: Category): Int
}
