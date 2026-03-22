package net.micode.spendingtracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.micode.spendingtracker.model.PeriodSummary

@Dao
interface PeriodSummaryDao {
    @Query("SELECT * FROM period_summaries WHERE summaryKey = :summaryKey LIMIT 1")
    fun getSummaryByKey(summaryKey: String): Flow<PeriodSummary?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSummary(summary: PeriodSummary)
}
