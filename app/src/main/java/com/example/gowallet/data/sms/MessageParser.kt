package com.example.gowallet.data.sms

import android.util.Log

data class TransactionData(
    val ref: String,
    val amount: Double,
    val person: String, // Renamed to match your UI 'person' field
    val date: String,
    val type: String
)

object MessageParser {
    // 1. M-PESA Personal/Business: Matches "RQK7... Confirmed. Ksh450.00 received from WILLICENT MBUGUA"
    private val mpesaReceivedRegex = Regex("""([A-Z0-9]{10})\sConfirmed\.\sKsh\s?([\d,]+\.\d{2})\sreceived\sfrom\s(.+?)(?:\s\d+|$)""", RegexOption.IGNORE_CASE)

    // 2. M-PESA Till/Paybill (Buy Goods): Matches "RQK7... Confirmed. Ksh100.00 paid to [Business Name]"
    private val mpesaMerchantRegex = Regex("""([A-Z0-9]{10})\sConfirmed\.\sKsh\s?([\d,]+\.\d{2})\spaid\sto\s(.+?)\.""", RegexOption.IGNORE_CASE)

    // 3. AIRTEL MONEY: Matches "Trans.ID: [ID] ... Amount: Ksh [Amt] from [Name]"
    private val airtelRegex = Regex("""ID:\s?(\w+).*?Amount:\s?Ksh\s?([\d,]+\.\d{2})\sfrom\s(.+?)(?:\son|$)""", RegexOption.IGNORE_CASE)

    // 4. BANK (Generic): Matches "Ksh\s?([\d,]+\.\d{2}).*?by\s(.+)"
    private val bankRegex = Regex("""Ksh\s?([\d,]+\.\d{2}).*?by\s(.+)""", RegexOption.IGNORE_CASE)

    fun parse(sender: String, body: String): TransactionData? {
        return try {
            when {
                sender.contains("MPESA", true) -> {
                    // Try Received Pattern first, then Merchant Pattern
                    val match = mpesaReceivedRegex.find(body) ?: mpesaMerchantRegex.find(body)
                    match?.let {
                        val (ref, amt, name) = it.destructured
                        TransactionData(ref, cleanAmount(amt), name.trim(), "Today", "MPESA")
                    }
                }
                sender.contains("AIRTEL", true) -> {
                    airtelRegex.find(body)?.let {
                        val (ref, amt, name) = it.destructured
                        TransactionData(ref, cleanAmount(amt), name.trim(), "Today", "AIRTEL")
                    }
                }
                // Checks for Family Bank, Equity (400000), or NCBA
                sender.contains("FamilyBank", true) || sender.contains("222111", true) || sender.contains("400000", true) -> {
                    bankRegex.find(body)?.let {
                        val (amt, name) = it.destructured
                        TransactionData("BANK-REF", cleanAmount(amt), name.trim(), "Today", "BANK")
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e("MessageParser", "Error parsing SMS: ${e.message}")
            null
        }
    }

    private fun cleanAmount(amt: String): Double = amt.replace(",", "").toDouble()
}