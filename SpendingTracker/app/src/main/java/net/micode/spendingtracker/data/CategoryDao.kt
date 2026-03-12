package net.micode.spendingtracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.micode.spendingtracker.model.Category

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Delete
    suspend fun deleteCategory(category: Category): Int

    @Update
    suspend fun updateCategory(category: Category): Int
}
