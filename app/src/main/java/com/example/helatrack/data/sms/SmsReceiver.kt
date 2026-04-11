package com.example.helatrack.data.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.helatrack.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    // Background scope for Room DB operations
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            val prefs = context.getSharedPreferences("GoWalletPrefs", Context.MODE_PRIVATE)
            val savedMethod = prefs.getString("selected_method", "") ?: ""
            val userIdentifier = prefs.getString("identifier", "") ?: ""

            // Initialize database access
            val db = AppDatabase.getDatabase(context)
            val dao = db.transactionDao()

            for (msg in messages) {
                val sender = msg.displayOriginatingAddress ?: ""
                val body = msg.messageBody ?: ""

                if (sender.contains(savedMethod, ignoreCase = true) || body.contains(userIdentifier)) {
                    val entity = MessageParser.parse(sender, body)
                    if (entity != null) {
                        // REPLACED TODO: Automated persistence
                        scope.launch {
                            dao.insertTransaction(entity)
                        }
                    }
                }
            }
        }
    }
}