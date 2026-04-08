package com.example.helatrack.model

data class Transaction(
    val id: String,          // e.g., RQK71234XX
    val amount: Double,      // e.g., 450.00
    val person: String,      // e.g., WILLICENT MBUGUA
    val date: String,        // e.g., 07/04/2026
    val time: String,        // e.g., 11:45 AM
    val type: TransactionType // MPESA, FAMILY_BANK, etc.
)

enum class TransactionType { MPESA, FAMILY_BANK, AIRTEL }