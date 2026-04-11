package com.example.helatrack.data.sms

import android.util.Log
import com.example.helatrack.data.local.TransactionEntity

object MessageParser {
    private val mpesaReceivedRegex = Regex("""([A-Z0-9]{10})\sConfirmed\.\sKsh\s?([\d,]+\.\d{2})\sreceived\sfrom\s(.+?)(?:\s\d+|$)""", RegexOption.IGNORE_CASE)
    private val mpesaMerchantRegex = Regex("""([A-Z0-9]{10})\sConfirmed\.\sKsh\s?([\d,]+\.\d{2})\spaid\sto\s(.+?)\.""", RegexOption.IGNORE_CASE)
    private val airtelRegex = Regex("""ID:\s?(\w+).*?Amount:\s?Ksh\s?([\d,]+\.\d{2})\sfrom\s(.+?)(?:\son|$)""", RegexOption.IGNORE_CASE)
    private val bankRegex = Regex("""Ksh\s?([\d,]+\.\d{2}).*?by\s(.+)""", RegexOption.IGNORE_CASE)

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
                sender.contains("FamilyBank", true) || sender.contains("222111", true) || sender.contains("400000", true) -> {
                    bankRegex.find(body)?.let {
                        val (amt, name) = it.destructured
                        // For banks without a clear ref in the regex, we generate a unique one
                        TransactionEntity(
                            ref = "BNK-${System.currentTimeMillis()}",
                            amount = cleanAmount(amt),
                            person = name.trim(),
                            category = "BANK",
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