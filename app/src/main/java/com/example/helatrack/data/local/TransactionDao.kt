package com.example.helatrack.data.local

import androidx.room.*
import com.example.helatrack.ui.insights.CustomerPaymentSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    // Dynamic query for reporting: Today, Weekly, or Monthly
    @Query("SELECT SUM(amount) FROM transactions WHERE timestamp >= :startTime")
    fun getTotalIncomeSince(startTime: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE timestamp >= :startTime")
    fun getMonthlyTotal(startTime: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE timestamp >= :startTime AND category = 'DIGITAL'")
    fun getDigitalTotal(startTime: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE timestamp >= :startTime AND category = 'CASH'")
    fun getCashTotal(startTime: Long): Flow<Double?>

    @Query("SELECT person as name, SUM(amount) as totalPaid FROM transactions WHERE timestamp >= :startTime GROUP BY person ORDER BY totalPaid DESC LIMIT 3")
    fun getTop3Customers(startTime: Long): Flow<List<CustomerPaymentSummary>>


    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}

