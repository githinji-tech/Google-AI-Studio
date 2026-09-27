package com.example.parser

import com.example.data.model.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

data class ParsedTransaction(
  val id: Long = System.currentTimeMillis() + (0..999).random(),
  var referenceCode: String = "",
  var type: String = "EXPENSE", // "EXPENSE" or "INCOME"
  var amount: Double = 0.0,
  var title: String = "",
  var category: String = "Uncategorized",
  var date: Long = System.currentTimeMillis(),
  var paymentMethod: String = "M-Pesa",
  var rawText: String = "",
  var isValid: Boolean = true,
  var isSelected: Boolean = true,
  var needsCategoryPrompt: Boolean = false
) {
  fun toEntity(): TransactionEntity {
    return TransactionEntity(
      type = type,
      amount = amount,
      title = title.ifBlank { if (type == "INCOME") "Money Received" else "Payment" },
      category = category.ifBlank { "Uncategorized" },
      date = date,
      paymentMethod = paymentMethod,
      referenceCode = referenceCode,
      notes = "Imported from $paymentMethod"
    )
  }
}

object MobileMoneyParser {

  // Regex patterns
  private val MPESA_CODE_REGEX = Pattern.compile("^([A-Z0-9]{8,12})\\s+Confirmed\\.", Pattern.CASE_INSENSITIVE)
  private val AIRTEL_CODE_REGEX = Pattern.compile("Txn\\s*ID:?\\s*([A-Za-z0-9.]+)", Pattern.CASE_INSENSITIVE)

  // Amount regex: matches Ksh 1,500.00 or Ksh. 250 or KES 300 or $45.00
  private val AMOUNT_REGEX = Pattern.compile("(?:Ksh\\.?|KES|\\$|UGX|TZS)?\\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)", Pattern.CASE_INSENSITIVE)

  fun parseMessages(rawInput: String): List<ParsedTransaction> {
    if (rawInput.isBlank()) return emptyList()

    val chunks = splitIntoMessageChunks(rawInput)
    val results = mutableListOf<ParsedTransaction>()

    for (chunk in chunks) {
      val parsed = parseSingleMessage(chunk.trim())
      if (parsed != null && parsed.amount > 0) {
        results.add(parsed)
      }
    }

    return results
  }

  private fun splitIntoMessageChunks(input: String): List<String> {
    // If input contains multiple M-Pesa confirmations
    val mpesaSplit = input.split(Regex("(?=[A-Z0-9]{8,12}\\s+Confirmed\\.)", RegexOption.IGNORE_CASE))
    if (mpesaSplit.size > 1) {
      return mpesaSplit.filter { it.isNotBlank() }
    }

    // If input contains multiple Airtel Txn ID
    val airtelSplit = input.split(Regex("(?=Txn\\s*ID:?)", RegexOption.IGNORE_CASE))
    if (airtelSplit.size > 1) {
      return airtelSplit.filter { it.isNotBlank() }
    }

    // Otherwise split by double newlines or single newlines if separated
    val lines = input.split(Regex("\n{2,}"))
    if (lines.size > 1) {
      return lines.filter { it.isNotBlank() }
    }

    return listOf(input)
  }

  fun parseSingleMessage(message: String): ParsedTransaction? {
    val clean = message.replace("\r", " ").replace("\n", " ").trim()
    if (clean.isBlank()) return null

    val isMpesa = clean.contains("M-PESA", ignoreCase = true) || clean.contains("Confirmed.", ignoreCase = true)
    val isAirtel = clean.contains("Airtel", ignoreCase = true) || clean.contains("Txn ID", ignoreCase = true)

    val method = when {
      isAirtel -> "Airtel Money"
      isMpesa -> "M-Pesa"
      else -> "Mobile Money"
    }

    // Extract reference code
    var refCode = ""
    val mpesaMatcher = MPESA_CODE_REGEX.matcher(clean)
    if (mpesaMatcher.find()) {
      refCode = mpesaMatcher.group(1).orEmpty().trim().trimEnd('.', ',')
    } else {
      val airtelMatcher = AIRTEL_CODE_REGEX.matcher(clean)
      if (airtelMatcher.find()) {
        refCode = airtelMatcher.group(1).orEmpty().trim().trimEnd('.', ',')
      }
    }

    // Determine type: INCOME vs EXPENSE
    val isIncome = clean.contains("received", ignoreCase = true) ||
      clean.contains("credited", ignoreCase = true) ||
      clean.contains("deposit", ignoreCase = true) ||
      clean.contains("cash in", ignoreCase = true)

    val type = if (isIncome) "INCOME" else "EXPENSE"

    // Extract Amount
    var amount = 0.0
    // Try matching specific phrases
    val amountPatterns = listOf(
      Pattern.compile("(?:received|sent|paid|transferred|bought|withdraw)\\s+(?:Ksh\\.?|KES|\\$)?\\s*([0-9,]+(?:\\.[0-9]{1,2})?)", Pattern.CASE_INSENSITIVE),
      Pattern.compile("(?:Ksh\\.?|KES|\\$)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)", Pattern.CASE_INSENSITIVE),
      AMOUNT_REGEX
    )

    for (pattern in amountPatterns) {
      val matcher = pattern.matcher(clean)
      if (matcher.find()) {
        val amountStr = matcher.group(1)?.replace(",", "")
        val parsed = amountStr?.toDoubleOrNull()
        if (parsed != null && parsed > 0) {
          amount = parsed
          break
        }
      }
    }

    // Extract Counterparty / Title
    var title = extractCounterparty(clean, isIncome)

    // Extract Date
    val date = extractDate(clean)

    // Suggest category
    val guessedCategory = guessCategory(title, clean, isIncome)
    val category = guessedCategory ?: if (isIncome) "Direct Transfer" else "Uncategorized"
    val needsCategoryPrompt = (!isIncome && guessedCategory == null)

    return ParsedTransaction(
      referenceCode = refCode,
      type = type,
      amount = amount,
      title = title,
      category = category,
      date = date,
      paymentMethod = method,
      rawText = clean,
      isValid = amount > 0,
      needsCategoryPrompt = needsCategoryPrompt
    )
  }

  private fun extractCounterparty(text: String, isIncome: Boolean): String {
    // Patterns for M-Pesa:
    // "sent to John Doe 0712345678"
    // "paid to Naivas Supermarket"
    // "received Ksh... from Jane Smith 07..."
    // "bought Ksh100.00 of airtime"
    // "Withdraw Ksh... from 123456 - Agent Name"
    // "sent to Kenya Power for account 1234"
    if (text.contains("airtime", ignoreCase = true)) {
      return "Airtime Purchase"
    }

    if (text.contains("withdraw", ignoreCase = true)) {
      val agentPattern = Pattern.compile("from\\s+([0-9]+(?:\\s*-\\s*[^.]+)?)(?:\\s+on|\\.)", Pattern.CASE_INSENSITIVE)
      val m = agentPattern.matcher(text)
      if (m.find()) return "Withdraw: " + m.group(1)?.trim()
      return "ATM / Agent Withdrawal"
    }

    val counterpartyPatterns = listOf(
      Pattern.compile("paid\\s+to\\s+([^.\\d]+?)(?:\\s+on|\\s+for|\\.)", Pattern.CASE_INSENSITIVE),
      Pattern.compile("sent\\s+to\\s+([^.\\d]+?)(?:\\s+[0-9]{8,12}|\\s+for|\\s+on|\\.)", Pattern.CASE_INSENSITIVE),
      Pattern.compile("transferred\\s+(?:to\\s+)?([^.\\d]+?)(?:\\s+[0-9]{8,12}|\\s+on|\\.)", Pattern.CASE_INSENSITIVE),
      Pattern.compile("from\\s+([^.\\d]+?)(?:\\s+[0-9]{8,12}|\\s+on|\\.)", Pattern.CASE_INSENSITIVE)
    )

    for (p in counterpartyPatterns) {
      val m = p.matcher(text)
      if (m.find()) {
        val raw = m.group(1)?.trim() ?: ""
        if (raw.isNotBlank() && raw.length > 2) {
          return cleanName(raw)
        }
      }
    }

    return if (isIncome) "Direct Transfer / Income" else "General Merchant"
  }

  private fun cleanName(name: String): String {
    var result = name.replace(Regex("(?:account|acc|for|on|at)\\b.*", RegexOption.IGNORE_CASE), "")
      .replace(Regex("[^a-zA-Z0-9\\s&-]", RegexOption.IGNORE_CASE), " ")
      .trim()
    return if (result.length > 35) result.take(35) + "..." else result
  }

  private fun extractDate(text: String): Long {
    // Looks for "on 24/9/26 at 2:30 PM" or "24/09/2026 14:20" or "2026-09-24"
    val datePatterns = listOf(
      "dd/MM/yy 'at' h:mm a",
      "dd/MM/yyyy 'at' h:mm a",
      "dd/MM/yyyy HH:mm",
      "dd/MM/yy HH:mm",
      "yyyy-MM-dd HH:mm",
      "yyyy-MM-dd"
    )

    val regexMatcher = Pattern.compile("on\\s+([0-9]{1,2}/[0-9]{1,2}/[0-9]{2,4}(?:\\s+at\\s+[0-9]{1,2}:[0-9]{2}\\s*(?:AM|PM)?)?)", Pattern.CASE_INSENSITIVE).matcher(text)
    if (regexMatcher.find()) {
      val rawDate = regexMatcher.group(1)?.trim().orEmpty()
      for (fmt in datePatterns) {
        try {
          val sdf = SimpleDateFormat(fmt, Locale.US)
          val parsed = sdf.parse(rawDate)
          if (parsed != null) return parsed.time
        } catch (_: Exception) {}
      }
    }

    return System.currentTimeMillis()
  }

  fun guessCategory(title: String, rawText: String, isIncome: Boolean): String? {
    if (isIncome) {
      val rLower = rawText.lowercase(Locale.ROOT)
      return when {
        Regex("\\b(salary|payroll|wages)\\b", RegexOption.IGNORE_CASE).containsMatchIn(rLower) -> "Salary"
        Regex("\\b(dividend|dividends|interest|shares|cdsc)\\b", RegexOption.IGNORE_CASE).containsMatchIn(rLower) -> "Investment"
        Regex("\\b(business|sale|sales|customer|invoice)\\b", RegexOption.IGNORE_CASE).containsMatchIn(rLower) -> "Business"
        rawText.contains("received from", ignoreCase = true) -> "Direct Transfer"
        else -> null
      }
    }

    // Strip M-Pesa/Airtel boilerplate text so boilerplate words don't trigger false matches
    val cleanContent = "$title $rawText"
      .replace(Regex("(?i)[A-Z0-9]{8,12}\\s+Confirmed\\.?"), " ")
      .replace(Regex("(?i)Confirmed\\b"), " ")
      .replace(Regex("(?i)New\\s+M-PESA\\s+balance\\s+is.*"), " ")
      .replace(Regex("(?i)Transaction\\s+cost.*"), " ")
      .replace(Regex("(?i)Txn\\s*ID:?.*"), " ")
      .replace(Regex("(?i)Balance:.*"), " ")
      .lowercase(Locale.ROOT)

    // Ambiguous digital platforms, app stores, and payment channels that don't indicate what was purchased
    // (e.g. Google Play Store could be TikTok coins, gaming, educational apps, in-app subscriptions, etc.)
    // If nothing indicates the specific item purchased, return null to prompt the user!
    val ambiguousPlatformsRegex = Regex("\\b(google\\s*play|play\\s*store|app\\s*store|apple\\.com|paypal|pochi\\s*la\\s*biashara)\\b", RegexOption.IGNORE_CASE)
    if (ambiguousPlatformsRegex.containsMatchIn(cleanContent)) {
      return null
    }

    // 1. Food & Dining / Groceries
    val foodRegex = Regex("\\b(supermarket|mart|grocer|groceries|naivas|carrefour|quickmart|chandana|cleanshelf|food|foods|restaurant|cafe|coffee|java|kfc|bakery|bakes|butchery|hotel|pizza|burger|canteen|shawarma|eatery)\\b", RegexOption.IGNORE_CASE)
    if (foodRegex.containsMatchIn(cleanContent)) {
      return "Food & Dining"
    }

    // 2. Transport
    val transportRegex = Regex("\\b(uber|bolt|little\\s*cab|matatu|bus|fare|fuel|petrol|diesel|shell|total|rubis|parking|tahmeed|modern\\s*coast)\\b", RegexOption.IGNORE_CASE)
    if (transportRegex.containsMatchIn(cleanContent)) {
      return "Transport"
    }

    // 3. Bills & Utilities
    val billsRegex = Regex("\\b(kplc|kenya\\s*power|stima|token|tokens|electricity|water|wifi|fibre|fiber|airtime|zuku|faiba|dstv|gotv|startimes|postpaid)\\b", RegexOption.IGNORE_CASE)
    if (billsRegex.containsMatchIn(cleanContent)) {
      return "Bills & Utilities"
    }

    // 4. Health - strictly word boundaries, avoiding substring 'med' matching 'Confirmed'
    val healthRegex = Regex("\\b(hospital|clinic|pharmacy|chemist|doctor|dentist|dental|healthcare|dispensary|laboratory|optician)\\b", RegexOption.IGNORE_CASE)
    if (healthRegex.containsMatchIn(cleanContent)) {
      return "Health"
    }

    // 5. Education
    val eduRegex = Regex("\\b(school|university|college|tuition|fees|library|bookshop|stationery|exam|kuccps|helb)\\b", RegexOption.IGNORE_CASE)
    if (eduRegex.containsMatchIn(cleanContent)) {
      return "Education"
    }

    // 6. Entertainment
    val entRegex = Regex("\\b(netflix|spotify|showmax|cinema|movie|movies|arcade|bowling|concert|club|lounge|pub|tiktok)\\b", RegexOption.IGNORE_CASE)
    if (entRegex.containsMatchIn(cleanContent)) {
      return "Entertainment"
    }

    // 7. Housing
    val housingRegex = Regex("\\b(rent|hostel|hostels|apartment|landlord|caretaker|bedsitter)\\b", RegexOption.IGNORE_CASE)
    if (housingRegex.containsMatchIn(cleanContent)) {
      return "Housing"
    }

    // 8. Shopping
    val shopRegex = Regex("\\b(boutique|clothes|shoes|fashion|wear|apparel|jumia|kilimall|mall|thrift|mtumba)\\b", RegexOption.IGNORE_CASE)
    if (shopRegex.containsMatchIn(cleanContent)) {
      return "Shopping"
    }

    // If nothing in the transaction has a keyword that tells what the transaction is for
    // (such as "GOOGLE PLAY STORE", "PAYPAL", "John Doe", generic till numbers)
    // Return null so the app asks the user for the category!
    return null
  }
}
