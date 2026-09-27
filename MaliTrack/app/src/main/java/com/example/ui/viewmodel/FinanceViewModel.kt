package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FinanceRepository
import com.example.data.PreferencesManager
import com.example.data.model.AffordVerdict
import com.example.data.model.AffordabilitySimulation
import com.example.data.model.BudgetEntity
import com.example.data.model.DailySpendMetrics
import com.example.data.model.RecurringExpenseEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.SpendingLeak
import com.example.data.model.TransactionEntity
import com.example.data.model.UserAccount
import com.example.parser.ParsedTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: FinanceRepository
  private val prefsManager = PreferencesManager(application)

  init {
    val dao = AppDatabase.getDatabase(application).financeDao()
    repository = FinanceRepository(dao)

    // Sanity check: Fix misclassified transactions from older versions
    // (e.g., Google Play Store or digital intermediaries misclassified as Health)
    viewModelScope.launch {
      try {
        val all = repository.getAllTransactionsList()
        all.forEach { tx ->
          val isGooglePlay = tx.title.contains("google play", ignoreCase = true) ||
              tx.title.contains("play store", ignoreCase = true) ||
              tx.notes.contains("google play", ignoreCase = true)
          if (isGooglePlay && (tx.category.equals("Health", ignoreCase = true) || tx.category.isBlank())) {
            repository.updateTransaction(tx.copy(category = "Uncategorized"))
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  // Selected calendar period (Month & Year)
  private val _selectedCalendar = MutableStateFlow(Calendar.getInstance())
  val selectedCalendar: StateFlow<Calendar> = _selectedCalendar.asStateFlow()

  // Currency
  private val _currency = MutableStateFlow("KSh")
  val currency: StateFlow<String> = _currency.asStateFlow()

  fun setCurrency(newCurrency: String) {
    _currency.value = newCurrency
  }

  // Student & Freemium monetization state
  private val _isPremium = MutableStateFlow(prefsManager.isPremium)
  val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

  private val _premiumPlan = MutableStateFlow(prefsManager.premiumPlan)
  val premiumPlan: StateFlow<String> = _premiumPlan.asStateFlow()

  private val _studentAllowance = MutableStateFlow(prefsManager.studentAllowance)
  val studentAllowance: StateFlow<Double> = _studentAllowance.asStateFlow()

  private val _affordChecksCount = MutableStateFlow(prefsManager.affordChecksCount)
  val affordChecksCount: StateFlow<Int> = _affordChecksCount.asStateFlow()

  fun togglePremium(enabled: Boolean) {
    prefsManager.isPremium = enabled
    _isPremium.value = enabled
  }

  fun unlockPremium(plan: String) {
    prefsManager.isPremium = true
    prefsManager.premiumPlan = plan
    _isPremium.value = true
    _premiumPlan.value = plan
  }

  fun setStudentAllowance(amount: Double) {
    prefsManager.studentAllowance = amount
    _studentAllowance.value = amount
  }

  fun useAffordCheck(): Boolean {
    val count = prefsManager.incrementAffordChecks()
    _affordChecksCount.value = count
    return isPremium.value || count <= PreferencesManager.MAX_FREE_AFFORD_CHECKS_PER_MONTH
  }

  // --- Authentication State & Actions ---
  private val _isLoggedIn = MutableStateFlow(prefsManager.isLoggedIn)
  val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

  private val _currentUser = MutableStateFlow(prefsManager.getCurrentUser())
  val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

  fun performLogin(emailOrPhone: String, pass: String): Result<UserAccount> {
    val res = prefsManager.loginUser(emailOrPhone, pass)
    res.onSuccess { user ->
      _currentUser.value = user
      _isLoggedIn.value = true
    }
    return res
  }

  fun performRegister(fullName: String, emailOrPhone: String, pass: String): Result<UserAccount> {
    val res = prefsManager.registerUser(fullName, emailOrPhone, pass)
    res.onSuccess { user ->
      _currentUser.value = user
      _isLoggedIn.value = true
    }
    return res
  }

  fun performLogout() {
    prefsManager.logoutUser()
    _currentUser.value = null
    _isLoggedIn.value = false
  }

  // --- Auto-Log Permissions & SMS Inbox Sync ---
  private val _hasRequestedAutoLogPerms = MutableStateFlow(prefsManager.hasRequestedAutoLogPerms)
  val hasRequestedAutoLogPerms: StateFlow<Boolean> = _hasRequestedAutoLogPerms.asStateFlow()

  fun markAutoLogPermsRequested() {
    prefsManager.hasRequestedAutoLogPerms = true
    _hasRequestedAutoLogPerms.value = true
  }

  suspend fun syncSmsInbox(): Int {
    return com.example.parser.SmsInboxReader.readAndImportSmsTransactions(getApplication())
  }

  // Room flows
  val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allBudgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allRecurring: StateFlow<List<RecurringExpenseEntity>> = repository.allRecurringExpenses
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allSavings: StateFlow<List<SavingsGoalEntity>> = repository.allSavingsGoals
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Transactions filtered for current selected month
  val currentMonthTransactions: StateFlow<List<TransactionEntity>> = combine(
    allTransactions,
    selectedCalendar
  ) { list, cal ->
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)
    list.filter { tx ->
      val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
      txCal.get(Calendar.YEAR) == year && txCal.get(Calendar.MONTH) == month
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun previousMonth() {
    val newCal = Calendar.getInstance().apply {
      timeInMillis = _selectedCalendar.value.timeInMillis
      add(Calendar.MONTH, -1)
    }
    _selectedCalendar.value = newCal
  }

  fun nextMonth() {
    val newCal = Calendar.getInstance().apply {
      timeInMillis = _selectedCalendar.value.timeInMillis
      add(Calendar.MONTH, 1)
    }
    _selectedCalendar.value = newCal
  }

  fun resetToCurrentMonth() {
    _selectedCalendar.value = Calendar.getInstance()
  }

  val selectedMonthName: String
    get() {
      val fmt = SimpleDateFormat("MMMM yyyy", Locale.US)
      return fmt.format(_selectedCalendar.value.time)
    }

  // Transaction operations
  fun addTransaction(
    type: String,
    amount: Double,
    title: String,
    category: String,
    date: Long,
    paymentMethod: String,
    referenceCode: String = "",
    notes: String = ""
  ) {
    viewModelScope.launch {
      val entity = TransactionEntity(
        type = type,
        amount = amount,
        title = title,
        category = category,
        date = date,
        paymentMethod = paymentMethod,
        referenceCode = referenceCode,
        notes = notes
      )
      repository.insertTransaction(entity)
    }
  }

  fun updateTransaction(tx: TransactionEntity) {
    viewModelScope.launch {
      repository.updateTransaction(tx)
    }
  }

  fun updateTransactionCategory(tx: TransactionEntity, newCategory: String, newNotes: String? = null) {
    viewModelScope.launch {
      val updatedNotes = if (!newNotes.isNullOrBlank()) newNotes else tx.notes
      repository.updateTransaction(tx.copy(category = newCategory, notes = updatedNotes))
    }
  }

  fun deleteTransaction(tx: TransactionEntity) {
    viewModelScope.launch {
      repository.deleteTransaction(tx)
    }
  }

  fun saveParsedTransactions(parsedList: List<ParsedTransaction>) {
    viewModelScope.launch {
      val entities = parsedList.filter { it.isSelected && it.isValid }.map { it.toEntity() }
      if (entities.isNotEmpty()) {
        repository.insertTransactions(entities)
      }
    }
  }

  // Budget operations
  fun saveBudget(category: String, limitAmount: Double) {
    viewModelScope.launch {
      val existing = allBudgets.value.firstOrNull { it.category == category }
      if (existing != null) {
        repository.updateBudget(existing.copy(limitAmount = limitAmount))
      } else {
        repository.insertBudget(BudgetEntity(category = category, limitAmount = limitAmount))
      }
    }
  }

  fun deleteBudget(budget: BudgetEntity) {
    viewModelScope.launch {
      repository.deleteBudget(budget)
    }
  }

  // Recurring expense operations
  fun addRecurringExpense(
    title: String,
    amount: Double,
    category: String,
    paymentMethod: String,
    frequency: String,
    dueDay: Int
  ) {
    viewModelScope.launch {
      val item = RecurringExpenseEntity(
        title = title,
        amount = amount,
        category = category,
        paymentMethod = paymentMethod,
        frequency = frequency,
        dueDay = dueDay
      )
      repository.insertRecurringExpense(item)
    }
  }

  fun logRecurringNow(item: RecurringExpenseEntity) {
    viewModelScope.launch {
      // 1. Add to transactions
      val tx = TransactionEntity(
        type = "EXPENSE",
        amount = item.amount,
        title = item.title,
        category = item.category,
        date = System.currentTimeMillis(),
        paymentMethod = item.paymentMethod,
        notes = "Recurring: ${item.frequency.lowercase().replaceFirstChar { it.titlecase() }}"
      )
      repository.insertTransaction(tx)

      // 2. Update last logged date
      repository.updateRecurringExpense(item.copy(lastLoggedDate = System.currentTimeMillis()))
    }
  }

  fun deleteRecurringExpense(item: RecurringExpenseEntity) {
    viewModelScope.launch {
      repository.deleteRecurringExpense(item)
    }
  }

  // Savings Goal operations
  fun addSavingsGoal(
    title: String,
    targetAmount: Double,
    currentAmount: Double,
    targetDate: Long? = null,
    notes: String = ""
  ) {
    viewModelScope.launch {
      val goal = SavingsGoalEntity(
        title = title,
        targetAmount = targetAmount,
        currentAmount = currentAmount,
        targetDate = targetDate,
        notes = notes
      )
      repository.insertSavingsGoal(goal)
    }
  }

  fun updateGoalSavings(goal: SavingsGoalEntity, delta: Double) {
    viewModelScope.launch {
      val newAmount = (goal.currentAmount + delta).coerceAtLeast(0.0)
      repository.updateSavingsGoal(goal.copy(currentAmount = newAmount))
    }
  }

  fun deleteSavingsGoal(goal: SavingsGoalEntity) {
    viewModelScope.launch {
      repository.deleteSavingsGoal(goal)
    }
  }

  // Pre-seed realistic mobile money sample data for Kenya/East Africa context
  fun populateSampleData() {
    viewModelScope.launch {
      val now = System.currentTimeMillis()
      val dayMillis = 86400000L

      val sampleTxs = listOf(
        TransactionEntity(
          type = "INCOME",
          amount = 85000.0,
          title = "Tech Works Salary Ltd",
          category = "Salary",
          date = now - (dayMillis * 18),
          paymentMethod = "Bank Transfer",
          referenceCode = "SAL9082341",
          notes = "Monthly tech consulting salary"
        ),
        TransactionEntity(
          type = "INCOME",
          amount = 12000.0,
          title = "James Mwangi",
          category = "Direct Transfer",
          date = now - (dayMillis * 5),
          paymentMethod = "M-Pesa",
          referenceCode = "QJ34XZ78PO",
          notes = "Project milestone payment"
        ),
        TransactionEntity(
          type = "EXPENSE",
          amount = 28000.0,
          title = "Apartment Rent",
          category = "Housing",
          date = now - (dayMillis * 17),
          paymentMethod = "M-Pesa",
          referenceCode = "QI12AA90BC",
          notes = "Paid via M-Pesa Paybill"
        ),
        TransactionEntity(
          type = "EXPENSE",
          amount = 4500.0,
          title = "Carrefour Hub",
          category = "Groceries",
          date = now - (dayMillis * 12),
          paymentMethod = "M-Pesa",
          referenceCode = "QH45BB87CD",
          notes = "Bi-weekly household groceries"
        ),
        TransactionEntity(
          type = "EXPENSE",
          amount = 3200.0,
          title = "Naivas Supermarket",
          category = "Food & Dining",
          date = now - (dayMillis * 3),
          paymentMethod = "Airtel Money",
          referenceCode = "AM260920.88",
          notes = "Weekly fresh food supplies"
        ),
        TransactionEntity(
          type = "EXPENSE",
          amount = 2500.0,
          title = "Kenya Power KPLC Prepaid",
          category = "Bills & Utilities",
          date = now - (dayMillis * 14),
          paymentMethod = "M-Pesa",
          referenceCode = "QG78CC65DE",
          notes = "Token units purchase"
        ),
        TransactionEntity(
          type = "EXPENSE",
          amount = 3999.0,
          title = "Safaricom Home Fibre",
          category = "Bills & Utilities",
          date = now - (dayMillis * 15),
          paymentMethod = "M-Pesa",
          referenceCode = "QF32DD54EF",
          notes = "Internet 50 Mbps"
        ),
        TransactionEntity(
          type = "EXPENSE",
          amount = 1450.0,
          title = "Uber Ride Westlands",
          category = "Transport",
          date = now - (dayMillis * 2),
          paymentMethod = "M-Pesa",
          referenceCode = "QE90EE43FG",
          notes = "Commute to meeting"
        ),
        TransactionEntity(
          type = "EXPENSE",
          amount = 3000.0,
          title = "TotalEnergies Fuel",
          category = "Transport",
          date = now - (dayMillis * 8),
          paymentMethod = "M-Pesa",
          referenceCode = "QD11FF32GH",
          notes = "Car refueling"
        ),
        TransactionEntity(
          type = "EXPENSE",
          amount = 1200.0,
          title = "Java House Coffee & Lunch",
          category = "Food & Dining",
          date = now - (dayMillis * 1),
          paymentMethod = "Cash",
          referenceCode = "",
          notes = "Lunch meeting"
        ),
        TransactionEntity(
          type = "EXPENSE",
          amount = 1100.0,
          title = "Netflix Premium",
          category = "Entertainment",
          date = now - (dayMillis * 10),
          paymentMethod = "Card",
          referenceCode = "NETFLIX44",
          notes = "Monthly streaming subscription"
        )
      )
      repository.insertTransactions(sampleTxs)

      // Sample Budgets
      repository.insertBudget(BudgetEntity(category = "ALL", limitAmount = 65000.0))
      repository.insertBudget(BudgetEntity(category = "Food & Dining", limitAmount = 15000.0))
      repository.insertBudget(BudgetEntity(category = "Transport", limitAmount = 10000.0))
      repository.insertBudget(BudgetEntity(category = "Bills & Utilities", limitAmount = 8000.0))
      repository.insertBudget(BudgetEntity(category = "Groceries", limitAmount = 12000.0))

      // Sample Recurring
      repository.insertRecurringExpense(
        RecurringExpenseEntity(
          title = "House Rent",
          amount = 28000.0,
          category = "Housing",
          paymentMethod = "M-Pesa",
          frequency = "MONTHLY",
          dueDay = 5
        )
      )
      repository.insertRecurringExpense(
        RecurringExpenseEntity(
          title = "Safaricom Fibre Internet",
          amount = 3999.0,
          category = "Bills & Utilities",
          paymentMethod = "M-Pesa",
          frequency = "MONTHLY",
          dueDay = 15
        )
      )
      repository.insertRecurringExpense(
        RecurringExpenseEntity(
          title = "Gym Membership",
          amount = 4000.0,
          category = "Health",
          paymentMethod = "M-Pesa",
          frequency = "MONTHLY",
          dueDay = 1
        )
      )

      // Sample Savings Goals
      repository.insertSavingsGoal(
        SavingsGoalEntity(
          title = "Emergency Fund (3 Months)",
          targetAmount = 150000.0,
          currentAmount = 95000.0,
          notes = "Safe cushion in Money Market Fund"
        )
      )
      repository.insertSavingsGoal(
        SavingsGoalEntity(
          title = "New Work Laptop",
          targetAmount = 120000.0,
          currentAmount = 45000.0,
          notes = "For freelancing and mobile app dev"
        )
      )
    }
  }

  fun calculateDailyMetrics(
    monthTransactions: List<TransactionEntity>,
    recurringList: List<RecurringExpenseEntity>,
    cal: Calendar
  ): DailySpendMetrics {
    val now = Calendar.getInstance()
    val isCurrentMonth = cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
        cal.get(Calendar.MONTH) == now.get(Calendar.MONTH)

    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDay = if (isCurrentMonth) now.get(Calendar.DAY_OF_MONTH) else 1
    // Days remaining including today
    val daysRemaining = if (isCurrentMonth) (daysInMonth - currentDay + 1).coerceAtLeast(1) else daysInMonth

    val totalIncome = monthTransactions.filter { it.isIncome }.sumOf { it.amount }
    val totalExpense = monthTransactions.filter { it.isExpense }.sumOf { it.amount }

    // If student set a specific allowance pool (e.g. 7500), use that or net balance
    val customPool = _studentAllowance.value
    val availablePool = if (customPool > 0) {
      (customPool - totalExpense).coerceAtLeast(0.0)
    } else {
      (totalIncome - totalExpense).coerceAtLeast(0.0)
    }

    // Upcoming recurring commitments that haven't been logged yet this month
    val upcomingCommitments = recurringList.filter { rec ->
      val lastLogged = rec.lastLoggedDate
      if (lastLogged == null) true
      else {
        val lastCal = Calendar.getInstance().apply { timeInMillis = lastLogged }
        !(lastCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR) && lastCal.get(Calendar.MONTH) == cal.get(Calendar.MONTH))
      }
    }.sumOf { it.amount }

    val uncommittedRemaining = (availablePool - upcomingCommitments).coerceAtLeast(0.0)
    val recommendedDailyAllowance = if (daysRemaining > 0) uncommittedRemaining / daysRemaining else 0.0

    // Today's spending
    val todayStartCal = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val todayEndCal = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 23)
      set(Calendar.MINUTE, 59)
      set(Calendar.SECOND, 59)
      set(Calendar.MILLISECOND, 999)
    }

    val todaySpent = if (isCurrentMonth) {
      monthTransactions.filter {
        it.isExpense && it.date in todayStartCal.timeInMillis..todayEndCal.timeInMillis
      }.sumOf { it.amount }
    } else 0.0

    val isOverToday = todaySpent > recommendedDailyAllowance
    val overAmount = if (isOverToday) todaySpent - recommendedDailyAllowance else 0.0
    val safeTodayRemaining = if (!isOverToday) (recommendedDailyAllowance - todaySpent).coerceAtLeast(0.0) else 0.0

    // Adjusted tomorrow daily allowance if today they overspent
    val daysAfterToday = (daysRemaining - 1).coerceAtLeast(1)
    val remainingAfterToday = (uncommittedRemaining - todaySpent).coerceAtLeast(0.0)
    val adjustedTomorrowDaily = remainingAfterToday / daysAfterToday

    // 3-day recent burn rate calculation
    val threeDaysAgo = System.currentTimeMillis() - (3L * 86400000L)
    val last3DaysSpent = monthTransactions.filter { it.isExpense && it.date >= threeDaysAgo }.sumOf { it.amount }
    val threeDayBurnRate = (last3DaysSpent / 3.0).coerceAtLeast(0.0)

    val effectiveBurnRate = if (threeDayBurnRate > 0) threeDayBurnRate else recommendedDailyAllowance
    val runwayDays = if (effectiveBurnRate > 0) (uncommittedRemaining / effectiveBurnRate).toInt() else daysRemaining

    return DailySpendMetrics(
      totalAvailable = availablePool,
      daysRemaining = daysRemaining,
      upcomingCommitments = upcomingCommitments,
      uncommittedRemaining = uncommittedRemaining,
      recommendedDailyAllowance = recommendedDailyAllowance,
      todaySpent = todaySpent,
      safeTodayRemaining = safeTodayRemaining,
      isOverToday = isOverToday,
      overAmount = overAmount,
      adjustedTomorrowDaily = adjustedTomorrowDaily,
      runwayDays = runwayDays,
      threeDayBurnRate = threeDayBurnRate
    )
  }

  fun simulatePurchaseImpact(
    cost: Double,
    itemName: String,
    currentMetrics: DailySpendMetrics
  ): AffordabilitySimulation {
    val daysRemaining = currentMetrics.daysRemaining.coerceAtLeast(1)
    val currentDaily = currentMetrics.recommendedDailyAllowance
    val remainingAfter = currentMetrics.uncommittedRemaining - cost

    val newDaily = if (remainingAfter > 0) remainingAfter / daysRemaining else 0.0
    val drop = (currentDaily - newDaily).coerceAtLeast(0.0)

    val verdict: AffordVerdict
    val explanation: String

    when {
      cost > currentMetrics.uncommittedRemaining -> {
        verdict = AffordVerdict.DANGEROUS
        explanation = "You cannot afford this right now. It exceeds your remaining uncommitted money by ${String.format(Locale.US, "%,.0f", cost - currentMetrics.uncommittedRemaining)}. It's best to pause, save up, or skip this purchase to keep your finances safe and stay debt-free."
      }
      newDaily < 150.0 -> {
        verdict = AffordVerdict.DANGEROUS
        explanation = "High risk of running broke! Buying this crushes your daily spending to ${String.format(Locale.US, "%,.0f", newDaily)}/day for the next $daysRemaining days."
      }
      currentDaily > 0 && drop / currentDaily > 0.35 -> {
        verdict = AffordVerdict.TIGHT_SQUEEZE
        explanation = "Tight squeeze! Cuts your daily margin by over 35%. You'll have ${String.format(Locale.US, "%,.0f", newDaily)}/day instead of ${String.format(Locale.US, "%,.0f", currentDaily)}/day."
      }
      else -> {
        verdict = AffordVerdict.SAFE
        explanation = "Safe to buy! You can afford it with ${String.format(Locale.US, "%,.0f", newDaily)}/day remaining for the next $daysRemaining days."
      }
    }

    return AffordabilitySimulation(
      itemName = itemName,
      cost = cost,
      currentDailyAllowance = currentDaily,
      newDailyAllowance = newDaily,
      dailyDrop = drop,
      remainingDays = daysRemaining,
      verdict = verdict,
      explanation = explanation
    )
  }

  fun getSpendingLeaks(monthTransactions: List<TransactionEntity>): List<SpendingLeak> {
    val expenseTxs = monthTransactions.filter { it.isExpense }
    val totalExpense = expenseTxs.sumOf { it.amount }
    if (totalExpense <= 0) return emptyList()

    val categoryTotals = expenseTxs.groupBy { it.category }
      .mapValues { entry -> entry.value.sumOf { it.amount } }

    val leaks = mutableListOf<SpendingLeak>()

    categoryTotals["Food & Dining"]?.let { spent ->
      if (spent >= 1200) {
        val saving = spent * 0.15
        leaks.add(
          SpendingLeak(
            category = "Food & Dining",
            spentAmount = spent,
            potentialSavings = saving,
            studentTip = "Cook in hostel or meal-prep 2 extra days weekly instead of takeaway.",
            monthsOfPremiumCovered = saving / 100.0
          )
        )
      }
    }

    categoryTotals["Entertainment"]?.let { spent ->
      if (spent >= 800) {
        val saving = spent * 0.20
        leaks.add(
          SpendingLeak(
            category = "Entertainment",
            spentAmount = spent,
            potentialSavings = saving,
            studentTip = "Share streaming logins or look for student free-entry campus events.",
            monthsOfPremiumCovered = saving / 100.0
          )
        )
      }
    }

    categoryTotals["Transport"]?.let { spent ->
      if (spent >= 1500) {
        val saving = spent * 0.10
        leaks.add(
          SpendingLeak(
            category = "Transport",
            spentAmount = spent,
            potentialSavings = saving,
            studentTip = "Take matatus early to beat surge fares, or walk short campus distances.",
            monthsOfPremiumCovered = saving / 100.0
          )
        )
      }
    }

    categoryTotals["Shopping"]?.let { spent ->
      if (spent >= 1000) {
        val saving = spent * 0.25
        leaks.add(
          SpendingLeak(
            category = "Shopping",
            spentAmount = spent,
            potentialSavings = saving,
            studentTip = "Implement a 48-hour cooling rule before purchasing non-academic items.",
            monthsOfPremiumCovered = saving / 100.0
          )
        )
      }
    }

    return leaks
  }

  fun clearAllData() {
    viewModelScope.launch {
      repository.clearAllTransactions()
    }
  }
}
