package net.micode.moneytracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.micode.moneytracker.model.PeriodSummary

/**
 * Data Access Object for the [PeriodSummary] entity.
 * Handles the persistence of financial summaries for specific time periods.
 */
@Dao
interface PeriodSummaryDao {
    /**
     * Retrieves a period summary by its unique key.
     * @param summaryKey The unique identifier for the summary.
     * @return A Flow that emits the [PeriodSummary] if found, or null otherwise.
     */
    @Query("SELECT * FROM period_summaries WHERE summaryKey = :summaryKey LIMIT 1")
    fun getSummaryByKey(summaryKey: String): Flow<PeriodSummary?>

    /**
     * Inserts or updates a period summary in the database.
     * @param summary The period summary to save.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSummary(summary: PeriodSummary)
}
