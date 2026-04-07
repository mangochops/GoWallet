package com.example.gowallet.data.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (msg in messages) {
                val sender = msg.displayOriginatingAddress ?: ""
                val body = msg.messageBody ?: ""

                val transaction = MessageParser.parse(sender, body)
                if (transaction != null) {
                    // TODO: Trigger your Supabase/Room save logic here
                    // e.g., Repository.saveTransaction(transaction)
                }
            }
        }
    }
}

