package com.example.helatrack.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.helatrack.R
import com.example.helatrack.data.local.AppDatabase
import java.util.Calendar

class EodSummaryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)

        // Calculate the start of today (00:00:00)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfToday = calendar.timeInMillis

        // Get all transactions for today
        val transactions = database.transactionDao().getTransactionsAfter(startOfToday)

        val totalIncome = transactions.sumOf { it.amount }
        val count = transactions.size

        if (count > 0) {
            sendNotification(
                "Daily Recap",
                "You collected KES ${String.format("%,.0f", totalIncome)} from $count transactions today!"
            )
        }

        return Result.success()
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "EOD_SUMMARY_CHANNEL"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Daily Summaries", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app icon
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}

