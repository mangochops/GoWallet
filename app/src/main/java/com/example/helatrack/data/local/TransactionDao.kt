package com.example.helatrack.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    // Dynamic query for reporting: Today, Weekly, or Monthly
    @Query("SELECT SUM(amount) FROM transactions WHERE timestamp >= :startTime")
    fun getTotalIncomeSince(startTime: Long): Flow<Double?>

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}

