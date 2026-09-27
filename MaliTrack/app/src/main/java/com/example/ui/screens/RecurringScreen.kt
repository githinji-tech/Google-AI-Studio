package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.CategoryHelper
import com.example.data.model.RecurringExpenseEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(
  recurringList: List<RecurringExpenseEntity>,
  currencySymbol: String,
  onAddRecurring: (
    title: String,
    amount: Double,
    category: String,
    paymentMethod: String,
    frequency: String,
    dueDay: Int
  ) -> Unit,
  onLogNow: (RecurringExpenseEntity) -> Unit,
  onDeleteRecurring: (RecurringExpenseEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  var showAddDialog by remember { mutableStateOf(false) }
  var titleInput by remember { mutableStateOf("") }
  var amountInput by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("Bills & Utilities") }
  var selectedMethod by remember { mutableStateOf("M-Pesa") }
  var selectedFrequency by remember { mutableStateOf("MONTHLY") }
  var dueDayInput by remember { mutableStateOf("1") }

  val totalMonthlyCommitment = recurringList.filter { it.isActive }.sumOf {
    when (it.frequency) {
      "WEEKLY" -> it.amount * 4.33
      "DAILY" -> it.amount * 30.0
      else -> it.amount
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Recurring Expenses",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
        }
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          titleInput = ""
          amountInput = ""
          dueDayInput = "1"
          showAddDialog = true
        },
        modifier = Modifier.testTag("add_recurring_fab"),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Recurring")
      }
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .padding(innerPadding),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Total Commitment Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "ESTIMATED MONTHLY COMMITMENTS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "$currencySymbol ${String.format(Locale.US, "%,.2f", totalMonthlyCommitment)}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
              )
            }
          }
        }
      }

      if (recurringList.isEmpty()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "No recurring expenses yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Add fixed monthly bills like Rent, Home Internet, Streaming, or School Fees.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("+ Add Recurring Bill")
              }
            }
          }
        }
      } else {
        items(recurringList, key = { it.id }) { item ->
          val meta = CategoryHelper.getCategoryMeta(item.category)
          val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
          val lastLoggedStr = if (item.lastLoggedDate != null) {
            "Last logged: " + dateFormat.format(Date(item.lastLoggedDate))
          } else {
            "Never logged this month"
          }

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
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(meta.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = meta.icon,
                      contentDescription = item.category,
                      tint = meta.color,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = item.title,
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "${item.frequency} • Due Day ${item.dueDay} • ${item.paymentMethod}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "$currencySymbol ${String.format(Locale.US, "%,.2f", item.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ExpenseRed
                  )
                  IconButton(onClick = { onDeleteRecurring(item) }) {
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

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = lastLoggedStr,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                  onClick = { onLogNow(item) },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.height(36.dp)
                ) {
                  Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Log Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(60.dp))
      }
    }
  }

  // Add Recurring Expense Dialog
  if (showAddDialog) {
    val categories = CategoryHelper.EXPENSE_CATEGORIES
    val methods = listOf("M-Pesa", "Airtel Money", "Cash", "Card", "Bank Transfer")
    val frequencies = listOf("MONTHLY", "WEEKLY")

    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = { Text("New Recurring Expense") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = titleInput,
            onValueChange = { titleInput = it },
            label = { Text("Name (e.g. WiFi Fibre)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          OutlinedTextField(
            value = amountInput,
            onValueChange = { amountInput = it },
            label = { Text("Amount ($currencySymbol)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = dueDayInput,
              onValueChange = { dueDayInput = it },
              label = { Text("Due Day (1-31)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp)
            )

            // Frequency
            Column(modifier = Modifier.weight(1f)) {
              Text("Frequency", style = MaterialTheme.typography.labelSmall)
              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                frequencies.forEach { freq ->
                  val isSel = selectedFrequency == freq
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                      .padding(horizontal = 8.dp, vertical = 6.dp)
                  ) {
                    Text(
                      text = freq.take(3),
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                  }
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val amt = amountInput.toDoubleOrNull() ?: 0.0
            val day = dueDayInput.toIntOrNull() ?: 1
            if (titleInput.isNotBlank() && amt > 0) {
              onAddRecurring(
                titleInput.trim(),
                amt,
                selectedCategory,
                selectedMethod,
                selectedFrequency,
                day
              )
              showAddDialog = false
            }
          },
          enabled = titleInput.isNotBlank() && (amountInput.toDoubleOrNull() ?: 0.0) > 0
        ) {
          Text("Save Recurring")
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
