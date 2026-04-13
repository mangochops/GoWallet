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
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            // Get user settings to filter relevant messages
            val prefs = context.getSharedPreferences("HelaTrackPrefs", Context.MODE_PRIVATE)
            val savedMethod = prefs.getString("selected_method", "") ?: ""
            val userIdentifier = prefs.getString("identifier", "") ?: ""

            val db = AppDatabase.getDatabase(context)
            val dao = db.transactionDao()

            for (msg in messages) {
                val sender = msg.displayOriginatingAddress ?: ""
                val body = msg.messageBody ?: ""

                // 1. Filter: Does this message belong to our tracked provider?
                if (sender.contains(savedMethod, ignoreCase = true) || body.contains(userIdentifier)) {

                    val entity = MessageParser.parse(sender, body)

                    if (entity != null) {
                        // 2. Refine Category: Ensure it's tagged for the Insights Progress Bar
                        val refinedCategory = when {
                            sender.contains("MPESA", ignoreCase = true) -> "MPESA"
                            sender.contains("EQUITY", ignoreCase = true) -> "BANK"
                            sender.contains("AIRTEL", ignoreCase = true) -> "AIRTEL"
                            // If the parser already found a specific bank name, keep it
                            entity.category != "OTHER" -> entity.category
                            else -> "DIGITAL" // Generic digital fallback
                        }

                        // 3. Persist with refined category
                        scope.launch {
                            dao.insertTransaction(entity.copy(category = refinedCategory))
                        }
                    }
                }
            }
        }
    }
}