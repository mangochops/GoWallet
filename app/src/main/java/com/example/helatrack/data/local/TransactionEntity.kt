package com.example.helatrack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val ref: String,        // Use official ID (e.g., RQK71234XX) as the PrimaryKey
    val amount: Double,
    val person: String,     // Matches your parser and 'person' field in UI
    val category: String,   // "MPESA", "AIRTEL", "BANK", or "CASH"
    val timestamp: Long = System.currentTimeMillis(),
    val rawMessage: String? = null // Optional: helpful for future audit debugging
)