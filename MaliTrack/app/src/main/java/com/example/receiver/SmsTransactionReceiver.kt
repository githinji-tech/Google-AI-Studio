package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.PreferencesManager
import com.example.parser.MobileMoneyParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsTransactionReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

    val prefs = PreferencesManager(context)
    if (!prefs.isAutoLogSmsEnabled) return

    val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
    if (messages.isEmpty()) return

    // Group message parts by originating address if multipart
    val fullBodyBuilder = StringBuilder()
    var sender = ""
    var timestamp = System.currentTimeMillis()

    for (msg in messages) {
      if (sender.isBlank()) sender = msg.originatingAddress.orEmpty()
      timestamp = msg.timestampMillis
      fullBodyBuilder.append(msg.messageBody)
    }

    val fullBody = fullBodyBuilder.toString()
    if (fullBody.isBlank()) return

    // Parse the mobile money / banking transaction
    val parsed = MobileMoneyParser.parseSingleMessage(fullBody) ?: return
    if (parsed.amount <= 0.0) return

    val pendingResult = goAsync()
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val db = AppDatabase.getDatabase(context)
        val dao = db.financeDao()

        // Check for duplicate reference code
        if (parsed.referenceCode.isNotBlank()) {
          val existing = dao.getTransactionByReference(parsed.referenceCode)
          if (existing != null) {
            pendingResult.finish()
            return@launch
          }
        }

        // Convert to entity and persist
        val entity = parsed.toEntity().copy(date = timestamp)
        dao.insertTransaction(entity)

        // Post a notification to let user know it was logged automatically
        showAutoLoggedNotification(context, entity.title, entity.amount, entity.type, entity.paymentMethod)
      } catch (e: Exception) {
        e.printStackTrace()
      } finally {
        pendingResult.finish()
      }
    }
  }

  private fun showAutoLoggedNotification(
    context: Context,
    title: String,
    amount: Double,
    type: String,
    method: String
  ) {
    try {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      val channelId = "malitrack_auto_logs"

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
          channelId,
          "MaliTrack Auto-Logged Transactions",
          NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
          description = "Alerts when incoming SMS or notifications are automatically recorded."
        }
        notificationManager.createNotificationChannel(channel)
      }

      val contentIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val typeLabel = if (type == "INCOME") "Money Received" else "Expense Logged"
      val formattedAmt = "KSh %,.2f".format(amount)

      val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.stat_notify_more)
        .setContentTitle("MaliTrack: $typeLabel ($formattedAmt)")
        .setContentText("$title via $method auto-logged to your records.")
        .setContentIntent(contentIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

      notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
    } catch (e: Exception) {
      // Notification may fail if permission is missing, ignore gracefully
    }
  }
}
