package com.example.helatrack.data.sms

import android.util.Log
import com.example.helatrack.data.local.TransactionEntity

object MessageParser {
    // This version stops capturing the name as soon as it hits " on" or a period
    private val mpesaReceivedRegex = Regex("""([A-Z0-9]{10})\sConfirmed\.\s(?:Ksh|KES)\s?([\d,]+\.\d{2})\sreceived\sfrom\s(.+?)(?:\son|\.|$|(?:\s\d+))""", RegexOption.IGNORE_CASE)
    private val mpesaMerchantRegex = Regex("""([A-Z0-9]{10})\sConfirmed\.\s(?:Ksh|KES)\s?([\d,]+\.\d{2})\spaid\sto\s(.+?)(?:\son|\.|$|(?:\s\d+))""", RegexOption.IGNORE_CASE)
    private val airtelRegex = Regex("""ID:\s?(\w+).*?Amount:\s(?:Ksh|KES)\s?([\d,]+\.\d{2})\sfrom\s(.+?)(?:\son|$)""", RegexOption.IGNORE_CASE)    // This captures: 1. Amount, 2. The Recipient/Description
    private val bankRegex = Regex("""(?:Ksh|KES)\s?([\d,]+\.\d{2})\sby\s(.+?)(?:\son|\.|$|(?:\s\d+))""", RegexOption.IGNORE_CASE)
    fun parse(sender: String, body: String): TransactionEntity? {
        return try {
            when {
                sender.contains("MPESA", true) -> {
                    val match = mpesaReceivedRegex.find(body) ?: mpesaMerchantRegex.find(body)
                    match?.let {
                        val (ref, amt, name) = it.destructured
                        TransactionEntity(
                            ref = ref,
                            amount = cleanAmount(amt),
                            person = name.trim(),
                            category = "MPESA",
                            rawMessage = body
                        )
                    }
                }
                sender.contains("AIRTEL", true) -> {
                    airtelRegex.find(body)?.let {
                        val (ref, amt, name) = it.destructured
                        TransactionEntity(
                            ref = ref,
                            amount = cleanAmount(amt),
                            person = name.trim(),
                            category = "AIRTEL",
                            rawMessage = body
                        )
                    }
                }

                sender.matches(Regex(".*(Equity|FamilyBank|247247|222111|400000).*", RegexOption.IGNORE_CASE)) -> {
                    bankRegex.find(body)?.let { match ->
                        val (amt, name) = match.destructured
                        // Determine a prefix based on the sender
                        val prefix = if (sender.contains("Equity", true) || sender.contains("247247")) "EQ-BNK-" else "BNK-"

                        TransactionEntity(
                            ref = "$prefix${System.currentTimeMillis()}",
                            amount = cleanAmount(amt),
                            person = name.trim().take(25), // Safety truncate for UI
                            category = "BANK",
                            timestamp = System.currentTimeMillis(),
                            rawMessage = body
                        )
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