package com.example.helatrack.data.sms

import android.util.Log
import com.example.helatrack.data.local.TransactionEntity

object MessageParser {
    // This version stops capturing the name as soon as it hits " on" or a period
    private val mpesaReceivedRegex = Regex("""([A-Z0-9]{10})\sConfirmed\.\s(?:Ksh|KES)\s?([\d,]+(?:\.\d{2})?)\sreceived\sfrom\s(.+?)(?:\son|\.|$|(?:\s\d+))""", RegexOption.IGNORE_CASE)
    private val mpesaMerchantRegex = Regex("""([A-Z0-9]{10})\sConfirmed\.\s(?:Ksh|KES)\s?([\d,]+(?:\.\d{2})?)\spaid\sto\s(.+?)(?:\son|\.|$|(?:\s\d+))""", RegexOption.IGNORE_CASE)
    private val airtelRegex = Regex("""ID:\s?(\w+).*?Amount:\s(?:Ksh|KES)\s?([\d,]+\.\d{2})\sfrom\s(.+?)(?:\son|$)""", RegexOption.IGNORE_CASE)    // This captures: 1. Amount, 2. The Recipient/Description

    private val bankPaybillRegex = Regex("""(?:KES|Ksh)\s?([\d,]+(?:\.\d{2})?)\s(?:received via Paybill|paid to|spent on).*?from\s(?:[\d]+)?\s?\((.+?)\)""", RegexOption.IGNORE_CASE)

    private val genericBankRegex = Regex("""(?:Ksh|KES)\s?([\d,]+(?:\.\d{2})?)\s(?:by|at|from)\s(.+?)(?:\son|\.|$|(?:\s\d+))""", RegexOption.IGNORE_CASE)
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

                sender.matches(Regex(".*(ABSA|Equity|FamilyBank|247247|222111|400000).*", RegexOption.IGNORE_CASE)) -> {
                    val match = bankPaybillRegex.find(body) ?: genericBankRegex.find(body)

                    match?.let {
                        val (amt, name) = it.destructured
                        val prefix = when {
                            sender.contains("Equity", true) -> "EQ-"
                            sender.contains("ABSA", true) || sender.contains("303030") -> "AB-"
                            else -> "BK-"
                        }

                        // Extract a reference if it exists (e.g., "Ref: ABC123") or generate one
                        val refMatch = Regex("""Ref:\s?(\w+)""", RegexOption.IGNORE_CASE).find(body)
                        val finalRef = refMatch?.groupValues?.get(1) ?: "$prefix${System.currentTimeMillis()}"

                        TransactionEntity(
                            ref = finalRef,
                            amount = cleanAmount(amt),
                            person = name.trim().take(25),
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