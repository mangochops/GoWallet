package com.example.gowallet.model

object MockData {
    // Centralized Transaction List
    val transactions = listOf(
        Transaction(
            id = "SCD12345",
            amount = 1500.0,
            person = "Mary Wangechi",
            date = "07 Apr",
            time = "09:00 AM",
            type = TransactionType.MPESA
        ),
        Transaction(
            id = "SCD67890",
            amount = 450.0,
            person = "Isaac Kipruto",
            date = "07 Apr",
            time = "12:45 PM",
            type = TransactionType.MPESA
        ),
        Transaction(
            id = "FAM-99212",
            amount = 5000.0,
            person = "Family Bank Deposit",
            date = "06 Apr",
            time = "10:30 AM",
            type = TransactionType.FAMILY_BANK
        ),
        Transaction(
            id = "SCD11223",
            amount = 2100.0,
            person = "Oscar Mutuku",
            date = "04 Apr",
            time = "01:20 PM",
            type = TransactionType.MPESA
        ),
        Transaction(
            id = "ART-44512",
            amount = 1200.0,
            person = "Airtel Buy Goods",
            date = "04 Apr",
            time = "04:00 PM",
            type = TransactionType.AIRTEL
        ),
        Transaction(
            id = "SCD11226",
            amount = 850.0,
            person = "William Samoei",
            date = "03 Apr",
            time = "08:15 AM",
            type = TransactionType.MPESA
        )
    )

    // Centralized Graph Data
    val weeklyIncome = listOf(2000.0, 4500.0, 1500.0, 7000.0, 3000.0, 5500.0, 4000.0)
    val weekDays = listOf("M", "T", "W", "T", "F", "S", "S")
}

