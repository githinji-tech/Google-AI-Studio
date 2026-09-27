package com.example.parser

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import com.example.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SmsInboxReader {

  suspend fun readAndImportSmsTransactions(context: Context, maxMessages: Int = 100): Int = withContext(Dispatchers.IO) {
    var importedCount = 0
    val contentResolver = context.contentResolver
    val inboxUri: Uri = Telephony.Sms.Inbox.CONTENT_URI

    val projection = arrayOf(
      Telephony.Sms._ID,
      Telephony.Sms.ADDRESS,
      Telephony.Sms.BODY,
      Telephony.Sms.DATE
    )

    var cursor: Cursor? = null
    try {
      cursor = contentResolver.query(
        inboxUri,
        projection,
        null,
        null,
        "${Telephony.Sms.DATE} DESC"
      )

      if (cursor != null && cursor.moveToFirst()) {
        val db = AppDatabase.getDatabase(context)
        val dao = db.financeDao()
        val addressCol = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
        val bodyCol = cursor.getColumnIndex(Telephony.Sms.BODY)
        val dateCol = cursor.getColumnIndex(Telephony.Sms.DATE)

        var processed = 0
        do {
          val address = if (addressCol >= 0) cursor.getString(addressCol).orEmpty() else ""
          val body = if (bodyCol >= 0) cursor.getString(bodyCol).orEmpty() else ""
          val date = if (dateCol >= 0) cursor.getLong(dateCol) else System.currentTimeMillis()

          if (body.isNotBlank()) {
            val parsed = MobileMoneyParser.parseSingleMessage(body)
            if (parsed != null && parsed.amount > 0.0) {
              var isDuplicate = false
              if (parsed.referenceCode.isNotBlank()) {
                val existing = dao.getTransactionByReference(parsed.referenceCode)
                if (existing != null) {
                  isDuplicate = true
                }
              }

              if (!isDuplicate) {
                val entity = parsed.toEntity().copy(
                  date = date,
                  notes = "Synced from SMS ($address)"
                )
                dao.insertTransaction(entity)
                importedCount++
              }
            }
          }
          processed++
        } while (cursor.moveToNext() && processed < maxMessages)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      cursor?.close()
    }

    importedCount
  }
}
