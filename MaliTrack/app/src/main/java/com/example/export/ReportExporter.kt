package com.example.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.TransactionEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

  fun exportToCsv(
    context: Context,
    transactions: List<TransactionEntity>,
    currencySymbol: String = "KSh"
  ): Uri? {
    try {
      val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
      val filename = "finance_report_${System.currentTimeMillis()}.csv"
      val cacheDir = File(context.cacheDir, "shared_reports")
      if (!cacheDir.exists()) cacheDir.mkdirs()

      val file = File(cacheDir, filename)
      val csvBuilder = StringBuilder()

      // CSV Header
      csvBuilder.append("ID,Date,Type,Amount ($currencySymbol),Category,Title / Counterparty,Payment Method,Reference Code,Notes\n")

      for (tx in transactions) {
        val dateStr = dateFormat.format(Date(tx.date))
        val escapedTitle = tx.title.replace("\"", "\"\"")
        val escapedCategory = tx.category.replace("\"", "\"\"")
        val escapedNotes = tx.notes.replace("\"", "\"\"")
        csvBuilder.append("${tx.id},\"$dateStr\",${tx.type},${tx.amount},\"$escapedCategory\",\"$escapedTitle\",${tx.paymentMethod},\"${tx.referenceCode}\",\"$escapedNotes\"\n")
      }

      val fos = FileOutputStream(file)
      fos.write(csvBuilder.toString().toByteArray())
      fos.close()

      return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
      )
    } catch (e: Exception) {
      e.printStackTrace()
      return null
    }
  }

  fun shareFile(context: Context, uri: Uri, mimeType: String = "text/csv", title: String = "Export Financial Report") {
    val intent = Intent(Intent.ACTION_SEND).apply {
      type = mimeType
      putExtra(Intent.EXTRA_STREAM, uri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, title))
  }

  fun generateSummaryText(
    transactions: List<TransactionEntity>,
    monthName: String,
    currencySymbol: String = "KSh"
  ): String {
    val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.isExpense }.sumOf { it.amount }
    val net = totalIncome - totalExpense

    val categoryBreakdown = transactions.filter { it.isExpense }
      .groupBy { it.category }
      .mapValues { entry -> entry.value.sumOf { it.amount } }
      .toList()
      .sortedByDescending { it.second }

    val sb = StringBuilder()
    sb.append("===============================\n")
    sb.append("PERSONAL FINANCE SUMMARY\n")
    sb.append("Period: $monthName\n")
    sb.append("===============================\n\n")
    sb.append("Total Income:   $currencySymbol ${String.format(Locale.US, "%,.2f", totalIncome)}\n")
    sb.append("Total Expenses: $currencySymbol ${String.format(Locale.US, "%,.2f", totalExpense)}\n")
    sb.append("Net Balance:    $currencySymbol ${String.format(Locale.US, "%,.2f", net)}\n")
    val savingsRate = if (totalIncome > 0) ((net / totalIncome) * 100).coerceAtLeast(0.0) else 0.0
    sb.append("Savings Rate:   ${String.format(Locale.US, "%.1f", savingsRate)}%\n\n")

    sb.append("SPENDING BY CATEGORY:\n")
    sb.append("-------------------------------\n")
    for ((category, amount) in categoryBreakdown) {
      val pct = if (totalExpense > 0) (amount / totalExpense) * 100 else 0.0
      sb.append("- $category: $currencySymbol ${String.format(Locale.US, "%,.2f", amount)} (${String.format(Locale.US, "%.1f", pct)}%)\n")
    }

    sb.append("\n===============================\n")
    sb.append("Generated with MaliTrack (Private & Offline)\n")
    return sb.toString()
  }

  fun shareSummaryText(context: Context, text: String, title: String = "Share Monthly Summary") {
    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_SUBJECT, "Financial Summary")
      putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, title))
  }
}
