package com.example.helatrack.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
object MockData {
    private val now = LocalDate.now()
    private val fmt = DateTimeFormatter.ofPattern("dd MMM")
    // Centralized Transaction List
    val transactions = listOf(
        Transaction(
            id = "SCD12345",
            amount = 1500.0,
            person = "Mary Wangechi",
            date = now.format(fmt),
            time = "09:00 AM",
            type = TransactionType.MPESA
        ),
        Transaction(
            id = "SCD67890",
            amount = 450.0,
            person = "Isaac Kipruto",
            date = now.minusDays(1).format(fmt),
            time = "12:45 PM",
            type = TransactionType.MPESA
        ),
        Transaction(
            id = "FAM-99212",
            amount = 5000.0,
            person = "Family Bank Deposit",
            date = now.minusDays(1).format(fmt),
            time = "10:30 AM",
            type = TransactionType.FAMILY_BANK
        ),
        Transaction(
            id = "SCD11223",
            amount = 2100.0,
            person = "Oscar Mutuku",
            date = now.minusDays(1).format(fmt),
            time = "01:20 PM",
            type = TransactionType.MPESA
        ),
        Transaction(
            id = "ART-44512",
            amount = 1200.0,
            person = "John Doe",
            date = now.minusDays(2).format(fmt),
            time = "04:00 PM",
            type = TransactionType.AIRTEL
        ),
        Transaction(
            id = "SCD11226",
            amount = 850.0,
            person = "William Samoei",
            date = now.minusDays(2).format(fmt),
            time = "08:15 AM",
            type = TransactionType.MPESA
        )
    )

    // Centralized Graph Data
    val weeklyIncome = listOf(2000.0, 4500.0, 1500.0, 7000.0, 3000.0, 5500.0, 4000.0)
    val weekDays = listOf("M", "T", "W", "T", "F", "S", "S")
}

