package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryHelper
import com.example.data.model.TransactionEntity
import com.example.ui.components.MonthSelectorHeader
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WarningAmber
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BudgetsScreen(
  monthName: String,
  budgets: List<BudgetEntity>,
  transactions: List<TransactionEntity>,
  currencySymbol: String,
  onPreviousMonth: () -> Unit,
  onNextMonth: () -> Unit,
  onCurrentMonth: () -> Unit,
  onSaveBudget: (category: String, limit: Double) -> Unit,
  onDeleteBudget: (BudgetEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  var showAddDialog by remember { mutableStateOf(false) }
  var selectedCategory by remember { mutableStateOf("ALL") }
  var limitInput by remember { mutableStateOf("") }

  val expenses = transactions.filter { it.isExpense }
  val totalExpense = expenses.sumOf { it.amount }

  val overallBudget = budgets.firstOrNull { it.category == "ALL" }
  val categoryBudgets = budgets.filter { it.category != "ALL" }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Budget Limits",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
        }
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          selectedCategory = "ALL"
          limitInput = ""
          showAddDialog = true
        },
        modifier = Modifier.testTag("add_budget_fab"),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White
      ) {
        Icon(Icons.Default.Add, contentDescription = "Set Budget")
      }
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .padding(innerPadding),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        MonthSelectorHeader(
          monthName = monthName,
          onPreviousMonth = onPreviousMonth,
          onNextMonth = onNextMonth,
          onCurrentMonth = onCurrentMonth
        )
      }

      // Overall Monthly Budget Hero Card
      item {
        val limit = overallBudget?.limitAmount ?: 0.0
        val spent = totalExpense
        val ratio = if (limit > 0) (spent / limit).toFloat().coerceIn(0f, 1f) else 0f
        val remaining = limit - spent
        val isOver = spent > limit && limit > 0
        val isNear = spent >= limit * 0.8 && !isOver

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "TOTAL MONTHLY BUDGET",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                  color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = if (limit > 0) "$currencySymbol ${String.format(Locale.US, "%,.0f", limit)}" else "Not Set",
                  style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }

              if (overallBudget != null) {
                IconButton(onClick = { onDeleteBudget(overallBudget) }) {
                  Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Budget",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                  )
                }
              }
            }

            if (limit > 0) {
              Spacer(modifier = Modifier.height(12.dp))
              LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(8.dp)
                  .clip(RoundedCornerShape(4.dp)),
                color = if (isOver) ExpenseRed else if (isNear) WarningAmber else IncomeGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
              )
              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "Spent: $currencySymbol ${String.format(Locale.US, "%,.0f", spent)} (${String.format(Locale.US, "%.0f", (spent / limit) * 100)}%)",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                  text = if (isOver) "Over by $currencySymbol ${String.format(Locale.US, "%,.0f", -remaining)}"
                  else "Left: $currencySymbol ${String.format(Locale.US, "%,.0f", remaining)}",
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                  color = if (isOver) ExpenseRed else if (isNear) WarningAmber else IncomeGreen
                )
              }
            } else {
              Spacer(modifier = Modifier.height(8.dp))
              TextButton(
                onClick = {
                  selectedCategory = "ALL"
                  limitInput = ""
                  showAddDialog = true
                }
              ) {
                Text("+ Set Monthly Spending Cap", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // Category Budgets Header
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Category Budgets (${categoryBudgets.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      if (categoryBudgets.isEmpty()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "No category budgets set yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(8.dp))
              Button(
                onClick = {
                  selectedCategory = "Food & Dining"
                  limitInput = ""
                  showAddDialog = true
                },
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Add Category Budget")
              }
            }
          }
        }
      } else {
        items(categoryBudgets, key = { it.id }) { b ->
          val meta = CategoryHelper.getCategoryMeta(b.category)
          val spentInCat = expenses.filter { it.category == b.category }.sumOf { it.amount }
          val ratio = (spentInCat / b.limitAmount).toFloat().coerceIn(0f, 1f)
          val isOver = spentInCat > b.limitAmount
          val isNear = spentInCat >= b.limitAmount * 0.8 && !isOver

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(34.dp)
                      .clip(CircleShape)
                      .background(meta.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = meta.icon,
                      contentDescription = b.category,
                      tint = meta.color,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = b.category,
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "$currencySymbol ${String.format(Locale.US, "%,.0f", spentInCat)} of $currencySymbol ${String.format(Locale.US, "%,.0f", b.limitAmount)}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (isOver || isNear) {
                    Icon(
                      imageVector = Icons.Default.Warning,
                      contentDescription = null,
                      tint = if (isOver) ExpenseRed else WarningAmber,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                  }
                  Text(
                    text = "${String.format(Locale.US, "%.0f", (spentInCat / b.limitAmount) * 100)}%",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isOver) ExpenseRed else if (isNear) WarningAmber else MaterialTheme.colorScheme.onSurface
                  )
                  IconButton(
                    onClick = { onDeleteBudget(b) },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      Icons.Default.Delete,
                      contentDescription = "Delete",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp)),
                color = if (isOver) ExpenseRed else if (isNear) WarningAmber else meta.color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
              )
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(60.dp))
      }
    }
  }

  // Add / Edit Budget Dialog
  if (showAddDialog) {
    val allOptions = listOf("ALL") + CategoryHelper.EXPENSE_CATEGORIES.map { it.name }

    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = { Text("Set Budget Limit") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Text(
            text = "Select Target",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
          )

          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            allOptions.forEach { opt ->
              val isSelected = selectedCategory == opt
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                  .clickable { selectedCategory = opt }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(
                  text = if (opt == "ALL") "Overall Total" else opt,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }

          OutlinedTextField(
            value = limitInput,
            onValueChange = { limitInput = it },
            label = { Text("Monthly Limit ($currencySymbol)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val limit = limitInput.toDoubleOrNull() ?: 0.0
            if (limit > 0) {
              onSaveBudget(selectedCategory, limit)
              showAddDialog = false
            }
          },
          enabled = (limitInput.toDoubleOrNull() ?: 0.0) > 0
        ) {
          Text("Save Budget")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
