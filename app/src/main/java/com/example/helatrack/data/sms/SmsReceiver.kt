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
import com.example.helatrack.R

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
                // 1. DYNAMIC TRACKING LOGIC
                // We track if it's a known provider OR if the text contains the user's specific ID (Till/Paybill)
                val supportedSenders = listOf("MPESA", "EQUITY", "ABSA", "AIRTEL", "303030", "AbsaAlert")
                val isKnownSender = supportedSenders.any { sender.contains(it, ignoreCase = true) }

                val isTracked = isKnownSender || (userIdentifier.isNotEmpty() && body.contains(userIdentifier, ignoreCase = true))

                if (isTracked) {
                    val entity = MessageParser.parse(sender, body)

                    if (entity != null) {
                        // 2. REFINE CATEGORY
                        // Map specific senders to the broad categories used in your UI (BANK, MPESA, etc.)
                        val refinedCategory = when {
                            sender.contains("MPESA", ignoreCase = true) -> "MPESA"
                            sender.contains("AIRTEL", ignoreCase = true) -> "AIRTEL"
                            sender.contains("EQUITY", ignoreCase = true) -> "BANK"
                            sender.contains("ABSA", ignoreCase = true) || sender.contains("303030") -> "BANK"
                            entity.category != "OTHER" -> entity.category
                            else -> "DIGITAL"
                        }

                        val finalEntity = entity.copy(category = refinedCategory)

                        scope.launch {
                            dao.insertTransaction(finalEntity)
                            showNotification(context, finalEntity)
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

    // 1. Format the amount to remove decimals for whole numbers
    val formattedAmount = if (transaction.amount % 1 == 0.0) {
        transaction.amount.toInt().toString() // Shows "1000" instead of "1000.0"
    } else {
        String.format("%.2f", transaction.amount) // Shows "1000.50" if there are cents
    }

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app icon
        .setContentTitle("New ${transaction.category} Transaction")
        .setContentText("KES $formattedAmount received from ${transaction.person}")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setNumber(1) // Tells the launcher to show a badge count
        .setAutoCancel(true)

    notificationManager.notify(transaction.timestamp.toInt(), builder.build())
}
