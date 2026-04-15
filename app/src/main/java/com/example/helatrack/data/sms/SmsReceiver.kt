package com.example.helatrack.data.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.helatrack.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.app.NotificationCompat
import com.example.helatrack.data.local.TransactionEntity
import android.app.NotificationManager
import android.os.Build
import android.app.NotificationChannel
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("HelaTrack_Debug", "onReceive triggered!")
        Log.d("HelaTrack_Debug", "Action: ${intent.action}")

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

                Log.d("SmsReceiver", "Received from: $sender") // Helpful for Logcat

                // FALLBACK: If prefs are empty, we allow MPESA for testing purposes
                val isTracked = (savedMethod.isNotEmpty() && sender.contains(savedMethod, true)) ||
                        (userIdentifier.isNotEmpty() && body.contains(userIdentifier)) ||
                        sender.contains("MPESA", true) // Added fallback for testing

                // 1. Filter: Does this message belong to our tracked provider?
                if (isTracked) {

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
                        val finalEntity = entity.copy(category = refinedCategory)

                        scope.launch {
                            dao.insertTransaction(finalEntity)
                            showNotification(
                                context,
                                finalEntity
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun showNotification(context: Context, transaction: TransactionEntity) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "transaction_alerts"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Transaction Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Shows notifications for new incoming transactions"
            setShowBadge(true) // CRITICAL: This enables the icon badge
        }
        notificationManager.createNotificationChannel(channel)
    }

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app icon
        .setContentTitle("New ${transaction.category} Transaction")
        .setContentText("KES ${transaction.amount} received from ${transaction.person}")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setNumber(1) // Tells the launcher to show a badge count
        .setAutoCancel(true)

    notificationManager.notify(transaction.timestamp.toInt(), builder.build())
}
