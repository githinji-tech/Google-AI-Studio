package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserAccount
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.Calendar
import java.util.UUID

class PreferencesManager(context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("malitrack_prefs", Context.MODE_PRIVATE)

  companion object {
    private const val KEY_IS_PREMIUM = "key_is_premium"
    private const val KEY_PREMIUM_PLAN = "key_premium_plan"
    private const val KEY_STUDENT_ALLOWANCE = "key_student_allowance"
    private const val KEY_AFFORD_CHECKS_COUNT = "key_afford_checks_count"
    private const val KEY_AFFORD_CHECKS_MONTH = "key_afford_checks_month"
    const val MAX_FREE_AFFORD_CHECKS_PER_MONTH = 3

    // Authentication keys
    private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
    private const val KEY_ACTIVE_USER_ID = "key_active_user_id"
    private const val KEY_USERS_JSON = "key_users_json"

    // Permissions & Auto-Logging status keys
    private const val KEY_AUTO_LOG_SMS_ENABLED = "key_auto_log_sms_enabled"
    private const val KEY_AUTO_LOG_NOTIFICATION_ENABLED = "key_auto_log_notification_enabled"
    private const val KEY_HAS_REQUESTED_AUTO_LOG_PERMS = "key_has_requested_auto_log_perms"
  }

  val currentMonthKey: String
    get() {
      val cal = Calendar.getInstance()
      return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}"
    }

  var isPremium: Boolean
    get() = prefs.getBoolean(KEY_IS_PREMIUM, false)
    set(value) = prefs.edit().putBoolean(KEY_IS_PREMIUM, value).apply()

  var premiumPlan: String
    get() = prefs.getString(KEY_PREMIUM_PLAN, "MONTHLY_100") ?: "MONTHLY_100"
    set(value) = prefs.edit().putString(KEY_PREMIUM_PLAN, value).apply()

  var studentAllowance: Double
    get() = prefs.getFloat(KEY_STUDENT_ALLOWANCE, 0f).toDouble()
    set(value) = prefs.edit().putFloat(KEY_STUDENT_ALLOWANCE, value.toFloat()).apply()

  var affordChecksCount: Int
    get() {
      checkAndResetMonthlyAffordChecks()
      return prefs.getInt(KEY_AFFORD_CHECKS_COUNT, 0)
    }
    set(value) {
      prefs.edit()
        .putInt(KEY_AFFORD_CHECKS_COUNT, value)
        .putString(KEY_AFFORD_CHECKS_MONTH, currentMonthKey)
        .apply()
    }

  fun checkAndResetMonthlyAffordChecks() {
    val recordedMonth = prefs.getString(KEY_AFFORD_CHECKS_MONTH, "")
    val thisMonth = currentMonthKey
    if (recordedMonth != thisMonth) {
      prefs.edit()
        .putString(KEY_AFFORD_CHECKS_MONTH, thisMonth)
        .putInt(KEY_AFFORD_CHECKS_COUNT, 0)
        .apply()
    }
  }

  fun incrementAffordChecks(): Int {
    checkAndResetMonthlyAffordChecks()
    val next = prefs.getInt(KEY_AFFORD_CHECKS_COUNT, 0) + 1
    prefs.edit()
      .putInt(KEY_AFFORD_CHECKS_COUNT, next)
      .putString(KEY_AFFORD_CHECKS_MONTH, currentMonthKey)
      .apply()
    return next
  }

  // --- Auto-logging & Permission State ---

  var isAutoLogSmsEnabled: Boolean
    get() = prefs.getBoolean(KEY_AUTO_LOG_SMS_ENABLED, true)
    set(value) = prefs.edit().putBoolean(KEY_AUTO_LOG_SMS_ENABLED, value).apply()

  var isAutoLogNotificationEnabled: Boolean
    get() = prefs.getBoolean(KEY_AUTO_LOG_NOTIFICATION_ENABLED, true)
    set(value) = prefs.edit().putBoolean(KEY_AUTO_LOG_NOTIFICATION_ENABLED, value).apply()

  var hasRequestedAutoLogPerms: Boolean
    get() = prefs.getBoolean(KEY_HAS_REQUESTED_AUTO_LOG_PERMS, false)
    set(value) = prefs.edit().putBoolean(KEY_HAS_REQUESTED_AUTO_LOG_PERMS, value).apply()

  // --- Authentication System ---

  var isLoggedIn: Boolean
    get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

  var activeUserId: String?
    get() = prefs.getString(KEY_ACTIVE_USER_ID, null)
    set(value) = prefs.edit().putString(KEY_ACTIVE_USER_ID, value).apply()

  fun hashPassword(password: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(password.toByteArray(Charsets.UTF_8))
    return digest.fold("") { str, it -> str + "%02x".format(it) }
  }

  fun getAllUsers(): List<UserAccount> {
    val jsonString = prefs.getString(KEY_USERS_JSON, null) ?: return emptyList()
    val list = mutableListOf<UserAccount>()
    try {
      val jsonArray = JSONArray(jsonString)
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        list.add(
          UserAccount(
            id = obj.getString("id"),
            fullName = obj.getString("fullName"),
            emailOrPhone = obj.getString("emailOrPhone"),
            passwordHash = obj.getString("passwordHash"),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
          )
        )
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return list
  }

  private fun saveAllUsers(users: List<UserAccount>) {
    val jsonArray = JSONArray()
    for (user in users) {
      val obj = JSONObject().apply {
        put("id", user.id)
        put("fullName", user.fullName)
        put("emailOrPhone", user.emailOrPhone)
        put("passwordHash", user.passwordHash)
        put("createdAt", user.createdAt)
      }
      jsonArray.put(obj)
    }
    prefs.edit().putString(KEY_USERS_JSON, jsonArray.toString()).apply()
  }

  fun registerUser(fullName: String, emailOrPhone: String, password: String): Result<UserAccount> {
    val cleanName = fullName.trim()
    val cleanIdentifier = emailOrPhone.trim().lowercase()

    if (cleanName.isBlank()) {
      return Result.failure(IllegalArgumentException("Please enter your full name"))
    }
    if (cleanIdentifier.isBlank()) {
      return Result.failure(IllegalArgumentException("Please enter an email or phone number"))
    }
    if (password.length < 4) {
      return Result.failure(IllegalArgumentException("Password must be at least 4 characters long"))
    }

    val users = getAllUsers().toMutableList()
    val exists = users.any { it.emailOrPhone.equals(cleanIdentifier, ignoreCase = true) }
    if (exists) {
      return Result.failure(IllegalArgumentException("An account with this email or phone already exists"))
    }

    val newUser = UserAccount(
      id = UUID.randomUUID().toString(),
      fullName = cleanName,
      emailOrPhone = cleanIdentifier,
      passwordHash = hashPassword(password),
      createdAt = System.currentTimeMillis()
    )

    users.add(newUser)
    saveAllUsers(users)

    // Mark as logged in
    activeUserId = newUser.id
    isLoggedIn = true

    return Result.success(newUser)
  }

  fun loginUser(emailOrPhone: String, password: String): Result<UserAccount> {
    val cleanIdentifier = emailOrPhone.trim().lowercase()
    if (cleanIdentifier.isBlank()) {
      return Result.failure(IllegalArgumentException("Please enter your email or phone number"))
    }
    if (password.isBlank()) {
      return Result.failure(IllegalArgumentException("Please enter your password"))
    }

    val users = getAllUsers()
    val user = users.firstOrNull { it.emailOrPhone.equals(cleanIdentifier, ignoreCase = true) }
      ?: return Result.failure(IllegalArgumentException("Account not found. Please create an account."))

    val inputHash = hashPassword(password)
    if (user.passwordHash != inputHash) {
      return Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
    }

    activeUserId = user.id
    isLoggedIn = true
    return Result.success(user)
  }

  fun logoutUser() {
    isLoggedIn = false
    activeUserId = null
  }

  fun getCurrentUser(): UserAccount? {
    if (!isLoggedIn) return null
    val currentId = activeUserId ?: return null
    return getAllUsers().firstOrNull { it.id == currentId }
  }
}
