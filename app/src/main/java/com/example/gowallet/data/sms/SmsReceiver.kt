package com.example.gowallet.data.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            // 1. Fetch your saved filter from Preferences (e.g., "M-PESA" or "EQUITY")
            val prefs = context.getSharedPreferences("GoWalletPrefs", Context.MODE_PRIVATE)
            val savedMethod = prefs.getString("selected_method", "") ?: ""
            val userIdentifier = prefs.getString("identifier", "") ?: ""

            for (msg in messages) {
                val sender = msg.displayOriginatingAddress ?: ""
                val body = msg.messageBody ?: ""

                if (sender.contains(savedMethod, ignoreCase = true) || body.contains(userIdentifier)) {
                    val transaction = MessageParser.parse(sender, body)
                    if (transaction != null) {
                        // TODO: Save to your local database/Supabase
                    }
                }
            }
        }
    }
}

