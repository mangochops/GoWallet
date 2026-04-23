package com.example.helatrack.features.transactions

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import android.content.Intent
import android.net.Uri
import com.example.helatrack.model.Transaction
import com.example.helatrack.data.local.TransactionEntity
import java.text.DateFormat
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat

fun exportTransactionsToPdf(context: Context, transactions: List<TransactionEntity>, businessName: String) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
    val page = pdfDocument.startPage(pageInfo)
    val canvas: Canvas = page.canvas
    val paint = Paint( )

    val dateFormatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

    var yPosition = 40f

    // Header
    paint.textSize = 18f
    paint.isFakeBoldText = true
    canvas.drawText("Transaction Report: $businessName", 40f, yPosition, paint)

    yPosition += 30f
    paint.textSize = 12f
    paint.isFakeBoldText = false
    canvas.drawText("Generated on: ${DateFormat.getDateTimeInstance().format(Date())}", 40f, yPosition, paint)

    yPosition += 40f
    // Table Headers
    canvas.drawText("Date", 40f, yPosition, paint)
    canvas.drawText("Description", 150f, yPosition, paint)
    canvas.drawText("Amount", 480f, yPosition, paint)

    yPosition += 10f
    canvas.drawLine(40f, yPosition, 550f, yPosition, paint)
    yPosition += 25f

    // Transaction Rows
    transactions.forEach { txn ->
        if (yPosition > 800f) return@forEach // Simple check for page overflow

        val dateString = dateFormatter.format(Date(txn.timestamp))
        canvas.drawText(dateString, 40f, yPosition, paint)
        canvas.drawText(txn.person, 150f, yPosition, paint)
        canvas.drawText("KES ${txn.amount}", 480f, yPosition, paint)
        yPosition += 20f
    }

    pdfDocument.finishPage(page)

    // Save to Cache and Share
    try {
        val file = File(context.cacheDir, "GoWallet_Report.pdf")
        pdfDocument.writeTo(FileOutputStream(file))

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Report"))
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to export: ${e.message}", Toast.LENGTH_SHORT).show()
    } finally {
        pdfDocument.close()
    }
}