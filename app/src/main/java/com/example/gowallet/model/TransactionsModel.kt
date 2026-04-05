package com.example.gowallet.model

data class Transaction(
    val id: String,
    val sender: String, // e.g., "M-PESA" or "TILL 123456"
    val amount: Double,
    val date: String,   // e.g., "05 Apr 2026"
    val time: String,   // e.g., "14:30"
    val reference: String // e.g., "RCV123456"
)

// This is our "Sample Data" list
val sampleTransactions = listOf(
    Transaction("1", "M-PESA", 1500.0, "05 Apr", "09:00", "SCD12345"),
    Transaction("2", "TILL 98765", 450.0, "05 Apr", "12:45", "SCD67890"),
    Transaction("3", "PAYBILL 400222", 2100.0, "04 Apr", "18:20", "SCD11223")
)