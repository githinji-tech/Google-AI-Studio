package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryHelper
import com.example.data.model.AffordabilitySimulation
import com.example.data.model.DailySpendMetrics
import com.example.data.model.SpendingLeak
import com.example.data.model.TransactionEntity
import com.example.export.ReportExporter
import com.example.ui.components.AutoLogPermissionCard
import com.example.ui.components.CategoryPromptDialog
import com.example.ui.components.EditTransactionDialog
import com.example.ui.components.FinancialSummaryCard
import com.example.ui.components.LeakDetectorCard
import com.example.ui.components.MonthSelectorHeader
import com.example.ui.components.SafeDailySpendHeroCard
import com.example.ui.components.UncategorizedPromptCard
import com.example.ui.theme.AirtelRed
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  monthName: String,
  transactions: List<TransactionEntity>,
  currencySymbol: String,
  overallBudget: BudgetEntity?,
  dailyMetrics: DailySpendMetrics,
  spendingLeaks: List<SpendingLeak>,
  isPremium: Boolean,
  affordChecksRemaining: Int,
  currentCustomAllowance: Double,
  onPreviousMonth: () -> Unit,
  onNextMonth: () -> Unit,
  onCurrentMonth: () -> Unit,
  onOpenAddExpense: () -> Unit,
  onOpenAddIncome: () -> Unit,
  onOpenParser: () -> Unit,
  onOpenSetBudget: () -> Unit,
  onDeleteTransaction: (TransactionEntity) -> Unit,
  onUpdateTransaction: (TransactionEntity) -> Unit = {},
  onCategorizeTransaction: (TransactionEntity, String, String?) -> Unit = { _, _, _ -> },
  onCurrencyChange: (String) -> Unit,
  onPopulateSampleData: () -> Unit,
  onClearData: () -> Unit,
  onLogout: () -> Unit = {},
  onSyncSmsInbox: () -> Unit = {},
  onSaveStudentAllowance: (Double) -> Unit,
  onUnlockPremium: (String) -> Unit,
  onTogglePremium: (Boolean) -> Unit,
  onLogQuickExpense: (String, Double) -> Unit,
  onUseAffordCheck: () -> Boolean = { true },
  onSimulatePurchase: (cost: Double, itemName: String, metrics: DailySpendMetrics) -> AffordabilitySimulation,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showMenu by remember { mutableStateOf(false) }
  var showCurrencyDialog by remember { mutableStateOf(false) }
  var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
  var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
  var promptingCategoryTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

  // Student Feature Dialog States
  var showAffordabilitySheet by remember { mutableStateOf(false) }
  var showSurvivalSheet by remember { mutableStateOf(false) }
  var showUpgradeSheet by remember { mutableStateOf(false) }
  var showAutoLogCard by remember { mutableStateOf(true) }

  val uncategorizedTransactions = transactions.filter { it.category.equals("Uncategorized", ignoreCase = true) }
  val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount }
  val totalExpense = transactions.filter { it.isExpense }.sumOf { it.amount }

  Column(modifier = modifier.fillMaxSize()) {
    // Top Bar
    TopAppBar(
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "MaliTrack",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = (-0.5).sp
            )
          )
        }
      },
      actions = {
        // Student Pro Badge / Upgrade Pill
        Surface(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { showUpgradeSheet = true }
            .testTag("home_pro_badge"),
          shape = RoundedCornerShape(8.dp),
          color = if (isPremium) IncomeGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.2f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (isPremium) Icons.Default.Shield else Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = if (isPremium) IncomeGreen else WarningAmber,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isPremium) "PRO" else "25 bob/wk",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp
              ),
              color = if (isPremium) IncomeGreen else WarningAmber
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Currency Selector Chip
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { showCurrencyDialog = true }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("currency_selector_chip")
        ) {
          Text(
            text = currencySymbol,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Export / Overflow menu
        IconButton(
          onClick = { showMenu = true },
          modifier = Modifier.testTag("home_overflow_menu")
        ) {
          Icon(Icons.Default.MoreVert, contentDescription = "More Options")
        }

        DropdownMenu(
          expanded = showMenu,
          onDismissRequest = { showMenu = false }
        ) {
          DropdownMenuItem(
            text = { Text("Export to CSV") },
            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
            onClick = {
              showMenu = false
              val uri = ReportExporter.exportToCsv(context, transactions, currencySymbol)
              if (uri != null) {
                ReportExporter.shareFile(context, uri, "text/csv", "Share Financial Report CSV")
              }
            }
          )
          DropdownMenuItem(
            text = { Text("Share Monthly Summary") },
            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
            onClick = {
              showMenu = false
              val summary = ReportExporter.generateSummaryText(transactions, monthName, currencySymbol)
              ReportExporter.shareSummaryText(context, summary, "Share Financial Summary")
            }
          )
          DropdownMenuItem(
            text = { Text("MaliTrack Student Pro") },
            leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
            onClick = {
              showMenu = false
              showUpgradeSheet = true
            }
          )
          DropdownMenuItem(
            text = { Text("Load Sample Data") },
            leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
            onClick = {
              showMenu = false
              onPopulateSampleData()
            }
          )
          DropdownMenuItem(
            text = { Text("Clear All Records") },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
            onClick = {
              showMenu = false
              onClearData()
            }
          )
          DropdownMenuItem(
            text = { Text("Log Out") },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
            onClick = {
              showMenu = false
              onLogout()
            }
          )
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background
      )
    )

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Month Selector
      item {
        MonthSelectorHeader(
          monthName = monthName,
          onPreviousMonth = onPreviousMonth,
          onNextMonth = onNextMonth,
          onCurrentMonth = onCurrentMonth
        )
      }

      // 1. PRIMARY DIFFERENTIATOR: SAFE DAILY SPEND HERO CARD
      item {
        SafeDailySpendHeroCard(
          metrics = dailyMetrics,
          currencySymbol = currencySymbol,
          isPremium = isPremium,
          affordChecksRemaining = affordChecksRemaining,
          onOpenAffordability = { showAffordabilitySheet = true },
          onOpenSurvivalGuide = { showSurvivalSheet = true },
          onOpenUpgrade = { showUpgradeSheet = true }
        )
      }

      // 1.4 AUTO-LOG SETUP CARD (grant SMS/notification access, sync inbox)
      if (showAutoLogCard) {
        item {
          AutoLogPermissionCard(
            onSyncInbox = onSyncSmsInbox,
            onDismiss = { showAutoLogCard = false },
            modifier = Modifier.padding(horizontal = 16.dp)
          )
        }
      }

      // 1.5 PROMPT FOR UNCATEGORIZED TRANSACTIONS (e.g. Google Play Store, TikTok coins, unknown transfers)
      if (uncategorizedTransactions.isNotEmpty()) {
        item {
          UncategorizedPromptCard(
            uncategorizedList = uncategorizedTransactions,
            currencySymbol = currencySymbol,
            onQuickCategorize = { tx, cat ->
              onCategorizeTransaction(tx, cat, null)
            },
            onOpenDetailedPrompt = { tx ->
              promptingCategoryTransaction = tx
            }
          )
        }
      }

      // 2. "SAVE MORE THAN YOU PAY" LEAK DETECTOR CARD
      item {
        LeakDetectorCard(
          leaks = spendingLeaks,
          currencySymbol = currencySymbol,
          isPremium = isPremium,
          onOpenUpgrade = { showUpgradeSheet = true }
        )
      }

      // 3. MONTHLY OVERVIEW (INCOME -> EXPENSES -> NET)
      item {
        FinancialSummaryCard(
          totalIncome = totalIncome,
          totalExpense = totalExpense,
          currencySymbol = currencySymbol,
          overallBudget = overallBudget,
          onSetBudgetClick = onOpenSetBudget
        )
      }

      // Quick Actions Row
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = onOpenAddExpense,
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .testTag("add_expense_button"),
            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Expense", fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = onOpenAddIncome,
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .testTag("add_income_button"),
            colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Income", fontWeight = FontWeight.Bold)
          }

          OutlinedButton(
            onClick = onOpenParser,
            modifier = Modifier
              .weight(1.3f)
              .height(44.dp)
              .testTag("paste_sms_button"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MpesaGreen)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Paste SMS", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
          }
        }
      }

      // Section Header: Recent Transactions
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Monthly Transactions (${transactions.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      // Transactions List or Empty State
      if (transactions.isEmpty()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "No records for $monthName",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Add manual expenses or paste M-Pesa / Airtel Money messages to start tracking.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = onPopulateSampleData,
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Load Student Sample Records")
              }
            }
          }
        }
      } else {
        items(transactions, key = { it.id }) { tx ->
          TransactionItemCard(
            tx = tx,
            currencySymbol = currencySymbol,
            onEdit = { editingTransaction = tx },
            onDelete = { transactionToDelete = tx },
            onPromptCategory = { promptingCategoryTransaction = tx }
          )
        }
      }

      // Spacer at bottom
      item {
        Spacer(modifier = Modifier.height(32.dp))
      }
    }
  }

  // Bottom Sheet 1: Can I Afford This?
  if (showAffordabilitySheet) {
    AffordabilitySimulatorSheet(
      metrics = dailyMetrics,
      currencySymbol = currencySymbol,
      isPremium = isPremium,
      affordChecksRemaining = affordChecksRemaining,
      onDismiss = { showAffordabilitySheet = false },
      onLogExpense = { name, cost ->
        onLogQuickExpense(name, cost)
      },
      onUseAffordCheck = onUseAffordCheck,
      onSimulate = onSimulatePurchase,
      onOpenUpgrade = {
        showAffordabilitySheet = false
        showUpgradeSheet = true
      }
    )
  }

  // Bottom Sheet 2: Survive Until Month-End
  if (showSurvivalSheet) {
    MonthEndSurvivalSheet(
      metrics = dailyMetrics,
      currencySymbol = currencySymbol,
      currentCustomAllowance = currentCustomAllowance,
      onSaveCustomAllowance = { newTarget ->
        onSaveStudentAllowance(newTarget)
      },
      onDismiss = { showSurvivalSheet = false }
    )
  }

  // Bottom Sheet 3: Student Pro Upgrade
  if (showUpgradeSheet) {
    PremiumUpgradeSheet(
      isCurrentlyPremium = isPremium,
      currencySymbol = currencySymbol,
      onDismiss = { showUpgradeSheet = false },
      onUnlockPremium = { plan ->
        onUnlockPremium(plan)
      },
      onTogglePremium = { enabled ->
        onTogglePremium(enabled)
      }
    )
  }

  // Delete Confirmation Dialog
  transactionToDelete?.let { tx ->
    AlertDialog(
      onDismissRequest = { transactionToDelete = null },
      title = { Text("Delete Transaction") },
      text = { Text("Are you sure you want to delete '${tx.title}' ($currencySymbol ${String.format(Locale.US, "%,.2f", tx.amount)})?") },
      confirmButton = {
        Button(
          onClick = {
            onDeleteTransaction(tx)
            transactionToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { transactionToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // Currency Selection Dialog
  if (showCurrencyDialog) {
    val currencies = listOf("KSh", "USD ($)", "EUR (€)", "GBP (£)", "UGX", "TZS", "RWF", "NGN (₦)", "ZAR (R)")
    AlertDialog(
      onDismissRequest = { showCurrencyDialog = false },
      title = { Text("Select Currency") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          currencies.forEach { curr ->
            val symbol = curr.substringBefore(" ")
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  onCurrencyChange(symbol)
                  showCurrencyDialog = false
                }
                .background(if (currencySymbol == symbol) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
              Text(
                text = curr,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = if (currencySymbol == symbol) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (currencySymbol == symbol) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showCurrencyDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Edit Transaction Dialog
  if (editingTransaction != null) {
    EditTransactionDialog(
      transaction = editingTransaction!!,
      currencySymbol = currencySymbol,
      onDismiss = { editingTransaction = null },
      onSave = { updated ->
        onUpdateTransaction(updated)
        editingTransaction = null
      },
      onDelete = {
        onDeleteTransaction(editingTransaction!!)
        editingTransaction = null
      }
    )
  }

  // Category Prompt Dialog for transactions without clear keywords (e.g. Google Play Store)
  if (promptingCategoryTransaction != null) {
    CategoryPromptDialog(
      transaction = promptingCategoryTransaction!!,
      currencySymbol = currencySymbol,
      onDismiss = { promptingCategoryTransaction = null },
      onSaveCategory = { tx, cat, note ->
        onCategorizeTransaction(tx, cat, note)
        val remaining = uncategorizedTransactions.filter { it.id != tx.id }
        promptingCategoryTransaction = remaining.firstOrNull()
      }
    )
  }
}

@Composable
fun TransactionItemCard(
  tx: TransactionEntity,
  currencySymbol: String,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onPromptCategory: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val meta = CategoryHelper.getCategoryMeta(tx.category)
  val isIncome = tx.isIncome
  val isUncategorized = tx.category.equals("Uncategorized", ignoreCase = true)

  val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.US)
  val dateString = dateFormat.format(Date(tx.date))

  val methodColor = when (tx.paymentMethod.uppercase()) {
    "M-PESA" -> MpesaGreen
    "AIRTEL MONEY" -> AirtelRed
    else -> MaterialTheme.colorScheme.onSurfaceVariant
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .clickable { if (isUncategorized) onPromptCategory() else onEdit() },
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Category Icon
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(meta.color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = meta.icon,
          contentDescription = tx.category,
          tint = meta.color,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Title, Category, Payment Method
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = tx.title,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = tx.category,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.width(6.dp))
          Box(
            modifier = Modifier
              .size(3.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.onSurfaceVariant)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = tx.paymentMethod,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = methodColor
          )
        }

        if (isUncategorized) {
          Spacer(modifier = Modifier.height(4.dp))
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = WarningAmber.copy(alpha = 0.2f),
            modifier = Modifier.clickable { onPromptCategory() }
          ) {
            Text(
              text = "⚠️ Needs Category • Tap to pick",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
              color = WarningAmber,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Text(
          text = dateString,
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
      }

      // Amount & Action buttons (Edit & Delete)
      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "${if (isIncome) "+" else "-"}$currencySymbol ${String.format(Locale.US, "%,.2f", tx.amount)}",
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
          color = if (isIncome) IncomeGreen else ExpenseRed
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onEdit,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit",
              tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
              modifier = Modifier.size(16.dp)
            )
          }

          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete",
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}
