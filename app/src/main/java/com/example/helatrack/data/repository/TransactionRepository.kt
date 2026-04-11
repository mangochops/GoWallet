package com.example.helatrack.data.repository

import com.example.helatrack.data.local.TransactionDao
import com.example.helatrack.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneOffset

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    // Automated Transaction (M-Pesa, Airtel, etc.)
    suspend fun insertTransaction(entity: TransactionEntity) {
        transactionDao.insertTransaction(entity)
    }

    // Manual Cash Sale Handling
    suspend fun addManualCashSale(amount: Double, note: String) {
        val cashRef = "CSH-${System.currentTimeMillis()}" // Generates unique audit ref
        val entity = TransactionEntity(
            ref = cashRef,
            amount = amount,
            person = "Cash Sale", // Standardized name for cash audits
            category = "CASH",
            timestamp = System.currentTimeMillis()
        )
        transactionDao.insertTransaction(entity)
    }

    fun getTodayTotal(): Flow<Double?> {
        val startOfDay = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        return transactionDao.getTotalIncomeSince(startOfDay)
    }

    fun getWeeklyTotal(): Flow<Double?> {
        val sevenDaysAgo = LocalDate.now().minusDays(7).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        return transactionDao.getTotalIncomeSince(sevenDaysAgo)
    }
}