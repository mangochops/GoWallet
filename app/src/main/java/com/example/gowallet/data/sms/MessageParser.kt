package com.example.gowallet.data.sms

data class TransactionData(
    val ref: String,
    val amount: Double,
    val name: String,
    val date: String,
    val type: String
)

object MessageParser {
    // Regex for M-Pesa 2026: Matches "RQK7... Confirmed. Ksh450.00 received from WILLICENT MBUGUA 0712***678"
    private val mpesaRegex = Regex("""([A-Z0-9]{10})\sConfirmed\.\sKsh([\d,]+\.\d{2})\sreceived\sfrom\s(.+?)\s\d{4}\*{3}\d{3}""")

    // Regex for Family Bank: Matches "Cash Deposit of Ksh 5,000.00 to Acc... by [Name]"
    private val familyBankRegex = Regex("""Ksh\s?([\d,]+\.\d{2}).*?by\s(.+)""", RegexOption.IGNORE_CASE)

    fun parse(sender: String, body: String): TransactionData? {
        return when {
            sender.contains("MPESA", true) -> {
                mpesaRegex.find(body)?.let {
                    val (ref, amt, name) = it.destructured
                    TransactionData(ref, amt.replace(",", "").toDouble(), name, "Today", "MPESA")
                }
            }
            sender.contains("FamilyBank", true) || sender.contains("222111", true) -> {
                familyBankRegex.find(body)?.let {
                    val (amt, name) = it.destructured
                    TransactionData("BANK-REF", amt.replace(",", "").toDouble(), name, "Today", "FAMILY_BANK")
                }
            }
            else -> null
        }
    }
}

