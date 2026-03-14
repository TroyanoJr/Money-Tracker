package net.micode.spendingtracker.data

import androidx.room.Database
import androidx.room.RoomDatabase
import net.micode.spendingtracker.model.Category
import net.micode.spendingtracker.model.Transaction

@Database(entities = [Transaction::class, Category::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
}
