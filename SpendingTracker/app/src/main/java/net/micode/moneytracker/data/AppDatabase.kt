package net.micode.moneytracker.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.micode.moneytracker.model.Account
import net.micode.moneytracker.model.Category
import net.micode.moneytracker.model.PeriodSummary
import net.micode.moneytracker.model.Transaction

/**
 * Main database class for the Spending Tracker application.
 * Defines the schema and provides access to the DAOs.
 * Uses Room for data persistence and supports migrations.
 */
@Database(
    entities = [Transaction::class, Category::class, PeriodSummary::class, Account::class], 
    version = 8, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    /** Returns the DAO for transaction operations. */
    abstract fun transactionDao(): TransactionDao
    
    /** Returns the DAO for category operations. */
    abstract fun categoryDao(): CategoryDao
    
    /** Returns the DAO for period summary operations. */
    abstract fun periodSummaryDao(): PeriodSummaryDao
    
    /** Returns the DAO for account operations. */
    abstract fun accountDao(): AccountDao

    companion object {
        /** Migration from version 4 to 5: Adds index to transactions date. */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_date ON transactions(date)")
            }
        }

        /** Migration from version 5 to 6: Creates the period_summaries table. */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS period_summaries (
                        summaryKey TEXT NOT NULL PRIMARY KEY,
                        period TEXT NOT NULL,
                        startDate INTEGER NOT NULL,
                        endDate INTEGER NOT NULL,
                        filterType TEXT NOT NULL,
                        categoryName TEXT,
                        totalIncome REAL NOT NULL,
                        totalExpense REAL NOT NULL,
                        balance REAL NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        /** Migration from version 6 to 7: Creates accounts table and adds accountId to transactions. */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS accounts (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, color INTEGER NOT NULL, isDefault INTEGER NOT NULL DEFAULT 0)")
                db.execSQL("INSERT INTO accounts (id, name, color, isDefault) VALUES (1, 'Default', -12303292, 1)")
                db.execSQL("ALTER TABLE transactions ADD COLUMN accountId INTEGER NOT NULL DEFAULT 1")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_accountId ON transactions(accountId)")
            }
        }

        /** Migration from version 7 to 8: Ensures Default account existence and transaction data integrity. */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Ensure Default account exists and transactions are linked correctly
                db.execSQL("INSERT OR IGNORE INTO accounts (id, name, color, isDefault) VALUES (1, 'Default', -12303292, 1)")
                db.execSQL("UPDATE accounts SET name = 'Default' WHERE id = 1")
                db.execSQL("UPDATE transactions SET accountId = 1 WHERE accountId = 0")
            }
        }
    }
}
