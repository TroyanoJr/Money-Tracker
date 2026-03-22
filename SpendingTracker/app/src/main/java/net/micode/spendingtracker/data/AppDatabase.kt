package net.micode.spendingtracker.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.micode.spendingtracker.model.Category
import net.micode.spendingtracker.model.PeriodSummary
import net.micode.spendingtracker.model.Transaction

@Database(entities = [Transaction::class, Category::class, PeriodSummary::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun periodSummaryDao(): PeriodSummaryDao

    companion object {
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_transactions_date ON transactions(date)"
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
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
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_period_summaries_period_dates ON period_summaries(period, startDate, endDate)"
                )
            }
        }
    }
}
