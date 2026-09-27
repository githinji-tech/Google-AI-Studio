package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.AutoLogPermissionDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ParserScreen
import com.example.ui.screens.PremiumUpgradeSheet
import com.example.ui.screens.RecurringScreen
import com.example.ui.screens.SavingsScreen
import com.example.ui.theme.FinanceTrackerTheme
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch

enum class ScreenTab {
  HOME,
  ANALYTICS,
  BUDGETS,
  RECURRING,
  SAVINGS,
  PARSER
}

class MainActivity : ComponentActivity() {
  private val viewModel: FinanceViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      FinanceTrackerTheme {
        FinanceApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun FinanceApp(viewModel: FinanceViewModel) {
  // Gate the whole app behind authentication
  val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

  if (!isLoggedIn) {
    AuthScreen(
      onLoginSuccess = { /* isLoggedIn flips automatically once login/register succeeds */ },
      onPerformLogin = { emailOrPhone, pass -> viewModel.performLogin(emailOrPhone, pass) },
      onPerformRegister = { fullName, emailOrPhone, pass ->
        viewModel.performRegister(fullName, emailOrPhone, pass)
      }
    )
    return
  }

  var currentTab by remember { mutableStateOf(ScreenTab.HOME) }
  var showAddTxDialog by remember { mutableStateOf(false) }
  var addTxInitialType by remember { mutableStateOf("EXPENSE") }
  var showGlobalUpgradeSheet by remember { mutableStateOf(false) }
  var showAutoLogDialog by remember { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()

  // Auto-Log permissions: prompt once, right after the user's first login/registration
  val hasRequestedAutoLogPerms by viewModel.hasRequestedAutoLogPerms.collectAsStateWithLifecycle()
  LaunchedEffect(hasRequestedAutoLogPerms) {
    if (!hasRequestedAutoLogPerms) {
      showAutoLogDialog = true
    }
  }

  fun syncSmsInbox() {
    coroutineScope.launch { viewModel.syncSmsInbox() }
  }

  // Observe flows with lifecycle
  val monthTransactions by viewModel.currentMonthTransactions.collectAsStateWithLifecycle()
  val budgets by viewModel.allBudgets.collectAsStateWithLifecycle()
  val recurringExpenses by viewModel.allRecurring.collectAsStateWithLifecycle()
  val savingsGoals by viewModel.allSavings.collectAsStateWithLifecycle()
  val currencySymbol by viewModel.currency.collectAsStateWithLifecycle()
  val selectedCal by viewModel.selectedCalendar.collectAsStateWithLifecycle()

  // Student monetization & paradigm metrics
  val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
  val studentAllowance by viewModel.studentAllowance.collectAsStateWithLifecycle()
  val affordChecksCount by viewModel.affordChecksCount.collectAsStateWithLifecycle()
  val affordChecksRemaining = if (isPremium) 999 else (com.example.data.PreferencesManager.MAX_FREE_AFFORD_CHECKS_PER_MONTH - affordChecksCount).coerceAtLeast(0)

  val dailyMetrics = viewModel.calculateDailyMetrics(monthTransactions, recurringExpenses, selectedCal)
  val spendingLeaks = viewModel.getSpendingLeaks(monthTransactions)

  val overallBudget = budgets.firstOrNull { it.category == "ALL" }

  // Handle Android back button on sub-screens
  if (currentTab != ScreenTab.HOME) {
    BackHandler {
      currentTab = ScreenTab.HOME
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    bottomBar = {
      if (currentTab != ScreenTab.PARSER) {
        NavigationBar {
          NavigationBarItem(
            selected = currentTab == ScreenTab.HOME,
            onClick = { currentTab = ScreenTab.HOME },
            icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Records") },
            label = { Text("Safe Spend") },
            modifier = Modifier.testTag("nav_records_tab")
          )
          NavigationBarItem(
            selected = currentTab == ScreenTab.ANALYTICS,
            onClick = { currentTab = ScreenTab.ANALYTICS },
            icon = { Icon(Icons.Default.PieChart, contentDescription = "Charts") },
            label = { Text("Insights") },
            modifier = Modifier.testTag("nav_charts_tab")
          )
          NavigationBarItem(
            selected = currentTab == ScreenTab.BUDGETS,
            onClick = { currentTab = ScreenTab.BUDGETS },
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Budgets") },
            label = { Text("Budgets") },
            modifier = Modifier.testTag("nav_budgets_tab")
          )
          NavigationBarItem(
            selected = currentTab == ScreenTab.RECURRING,
            onClick = { currentTab = ScreenTab.RECURRING },
            icon = { Icon(Icons.Default.Repeat, contentDescription = "Recurring") },
            label = { Text("Bills") },
            modifier = Modifier.testTag("nav_recurring_tab")
          )
          NavigationBarItem(
            selected = currentTab == ScreenTab.SAVINGS,
            onClick = { currentTab = ScreenTab.SAVINGS },
            icon = { Icon(Icons.Default.Savings, contentDescription = "Savings") },
            label = { Text("Goals") },
            modifier = Modifier.testTag("nav_savings_tab")
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentTab) {
        ScreenTab.HOME -> {
          HomeScreen(
            monthName = viewModel.selectedMonthName,
            transactions = monthTransactions,
            currencySymbol = currencySymbol,
            overallBudget = overallBudget,
            dailyMetrics = dailyMetrics,
            spendingLeaks = spendingLeaks,
            isPremium = isPremium,
            affordChecksRemaining = affordChecksRemaining,
            currentCustomAllowance = studentAllowance,
            onPreviousMonth = { viewModel.previousMonth() },
            onNextMonth = { viewModel.nextMonth() },
            onCurrentMonth = { viewModel.resetToCurrentMonth() },
            onOpenAddExpense = {
              addTxInitialType = "EXPENSE"
              showAddTxDialog = true
            },
            onOpenAddIncome = {
              addTxInitialType = "INCOME"
              showAddTxDialog = true
            },
            onOpenParser = { currentTab = ScreenTab.PARSER },
            onOpenSetBudget = { currentTab = ScreenTab.BUDGETS },
            onDeleteTransaction = { viewModel.deleteTransaction(it) },
            onUpdateTransaction = { viewModel.updateTransaction(it) },
            onCategorizeTransaction = { tx, cat, note -> viewModel.updateTransactionCategory(tx, cat, note) },
            onCurrencyChange = { viewModel.setCurrency(it) },
            onPopulateSampleData = { viewModel.populateSampleData() },
            onClearData = { viewModel.clearAllData() },
            onLogout = { viewModel.performLogout() },
            onSyncSmsInbox = { syncSmsInbox() },
            onSaveStudentAllowance = { viewModel.setStudentAllowance(it) },
            onUnlockPremium = { viewModel.unlockPremium(it) },
            onTogglePremium = { viewModel.togglePremium(it) },
            onLogQuickExpense = { name, cost ->
              viewModel.addTransaction(
                type = "EXPENSE",
                amount = cost,
                title = name,
                category = "Shopping",
                date = System.currentTimeMillis(),
                paymentMethod = "Cash",
                notes = "Recorded via 'Can I Afford This?'"
              )
            },
            onUseAffordCheck = { viewModel.useAffordCheck() },
            onSimulatePurchase = { cost, itemName, metrics ->
              viewModel.simulatePurchaseImpact(cost, itemName, metrics)
            }
          )
        }

        ScreenTab.ANALYTICS -> {
          AnalyticsScreen(
            monthName = viewModel.selectedMonthName,
            transactions = monthTransactions,
            calendar = selectedCal,
            currencySymbol = currencySymbol,
            spendingLeaks = spendingLeaks,
            isPremium = isPremium,
            onOpenUpgrade = { showGlobalUpgradeSheet = true },
            onPreviousMonth = { viewModel.previousMonth() },
            onNextMonth = { viewModel.nextMonth() },
            onCurrentMonth = { viewModel.resetToCurrentMonth() }
          )
        }

        ScreenTab.BUDGETS -> {
          BudgetsScreen(
            monthName = viewModel.selectedMonthName,
            budgets = budgets,
            transactions = monthTransactions,
            currencySymbol = currencySymbol,
            onPreviousMonth = { viewModel.previousMonth() },
            onNextMonth = { viewModel.nextMonth() },
            onCurrentMonth = { viewModel.resetToCurrentMonth() },
            onSaveBudget = { cat, limit -> viewModel.saveBudget(cat, limit) },
            onDeleteBudget = { viewModel.deleteBudget(it) }
          )
        }

        ScreenTab.RECURRING -> {
          RecurringScreen(
            recurringList = recurringExpenses,
            currencySymbol = currencySymbol,
            onAddRecurring = { title, amt, cat, method, freq, day ->
              viewModel.addRecurringExpense(title, amt, cat, method, freq, day)
            },
            onLogNow = { viewModel.logRecurringNow(it) },
            onDeleteRecurring = { viewModel.deleteRecurringExpense(it) }
          )
        }

        ScreenTab.SAVINGS -> {
          SavingsScreen(
            savingsGoals = savingsGoals,
            currencySymbol = currencySymbol,
            onAddGoal = { title, target, current, notes ->
              viewModel.addSavingsGoal(title, target, current, null, notes)
            },
            onUpdateSavings = { goal, delta ->
              viewModel.updateGoalSavings(goal, delta)
            },
            onDeleteGoal = { viewModel.deleteSavingsGoal(it) }
          )
        }

        ScreenTab.PARSER -> {
          ParserScreen(
            onBack = { currentTab = ScreenTab.HOME },
            onImportTransactions = { parsed ->
              viewModel.saveParsedTransactions(parsed)
            },
            currencySymbol = currencySymbol
          )
        }
      }
    }
  }

  // Global Upgrade Sheet (when invoked from Analytics or anywhere else)
  if (showGlobalUpgradeSheet) {
    PremiumUpgradeSheet(
      isCurrentlyPremium = isPremium,
      currencySymbol = currencySymbol,
      onDismiss = { showGlobalUpgradeSheet = false },
      onUnlockPremium = { plan ->
        viewModel.unlockPremium(plan)
      },
      onTogglePremium = { enabled ->
        viewModel.togglePremium(enabled)
      }
    )
  }

  // One-time onboarding prompt: ask to read SMS & notifications so transactions auto-log
  if (showAutoLogDialog) {
    AutoLogPermissionDialog(
      onDismiss = {
        showAutoLogDialog = false
        viewModel.markAutoLogPermsRequested()
      },
      onPermissionsGranted = {
        syncSmsInbox()
      }
    )
  }

  // Dialog for Adding manual Expense or Income
  if (showAddTxDialog) {
    AddTransactionDialog(
      initialType = addTxInitialType,
      currencySymbol = currencySymbol,
      onDismiss = { showAddTxDialog = false },
      onSave = { type, amount, title, category, date, method, refCode, notes ->
        viewModel.addTransaction(
          type = type,
          amount = amount,
          title = title,
          category = category,
          date = date,
          paymentMethod = method,
          referenceCode = refCode,
          notes = notes
        )
        showAddTxDialog = false
      }
    )
  }
}
