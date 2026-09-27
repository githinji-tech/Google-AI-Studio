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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Savings
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
import com.example.data.model.SavingsGoalEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(
  savingsGoals: List<SavingsGoalEntity>,
  currencySymbol: String,
  onAddGoal: (title: String, targetAmount: Double, initialAmount: Double, notes: String) -> Unit,
  onUpdateSavings: (goal: SavingsGoalEntity, delta: Double) -> Unit,
  onDeleteGoal: (SavingsGoalEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  var showAddDialog by remember { mutableStateOf(false) }
  var goalToAdjust by remember { mutableStateOf<SavingsGoalEntity?>(null) }
  var adjustDelta by remember { mutableStateOf("") }
  var isDeposit by remember { mutableStateOf(true) }

  val totalSaved = savingsGoals.sumOf { it.currentAmount }
  val totalTarget = savingsGoals.sumOf { it.targetAmount }
  val overallProgress = if (totalTarget > 0) (totalSaved / totalTarget).toFloat().coerceIn(0f, 1f) else 0f

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Savings Goals",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
        }
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddDialog = true },
        modifier = Modifier.testTag("add_savings_fab"),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White
      ) {
        Icon(Icons.Default.Add, contentDescription = "New Goal")
      }
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .padding(innerPadding),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Total Savings Header Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
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
                  text = "TOTAL SAVED FUNDS",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                  color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "$currencySymbol ${String.format(Locale.US, "%,.2f", totalSaved)}",
                  style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }

              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(IncomeGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Savings,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(24.dp)
                )
              }
            }

            if (totalTarget > 0) {
              Spacer(modifier = Modifier.height(14.dp))
              LinearProgressIndicator(
                progress = { overallProgress },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(8.dp)
                  .clip(RoundedCornerShape(4.dp)),
                color = IncomeGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
              )
              Spacer(modifier = Modifier.height(6.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "${String.format(Locale.US, "%.0f", overallProgress * 100)}% of total targets",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                  text = "Target: $currencySymbol ${String.format(Locale.US, "%,.0f", totalTarget)}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }
            }
          }
        }
      }

      if (savingsGoals.isEmpty()) {
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
                text = "No savings goals yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Set a goal for an Emergency Fund, new gadget, car deposit, or vacation.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("+ Create Savings Goal")
              }
            }
          }
        }
      } else {
        items(savingsGoals, key = { it.id }) { goal ->
          val isDone = goal.isCompleted
          val pct = goal.progress * 100

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (isDone) {
                    Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = "Completed",
                      tint = IncomeGreen,
                      modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                  }
                  Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }

                IconButton(
                  onClick = { onDeleteGoal(goal) },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Goal",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                  )
                }
              }

              if (goal.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = goal.notes,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              Spacer(modifier = Modifier.height(12.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
              ) {
                Column {
                  Text(
                    text = "Saved Amount",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = "$currencySymbol ${String.format(Locale.US, "%,.2f", goal.currentAmount)}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = IncomeGreen
                  )
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = "Target: $currencySymbol ${String.format(Locale.US, "%,.0f", goal.targetAmount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = "${String.format(Locale.US, "%.1f", pct)}%",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isDone) IncomeGreen else MaterialTheme.colorScheme.primary
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              LinearProgressIndicator(
                progress = { goal.progress },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(8.dp)
                  .clip(RoundedCornerShape(4.dp)),
                color = if (isDone) IncomeGreen else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
              )

              Spacer(modifier = Modifier.height(14.dp))

              // Quick Deposit / Withdraw Buttons
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Button(
                  onClick = {
                    goalToAdjust = goal
                    isDeposit = true
                    adjustDelta = ""
                  },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Add Funds", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                  onClick = {
                    goalToAdjust = goal
                    isDeposit = false
                    adjustDelta = ""
                  },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Withdraw", fontWeight = FontWeight.SemiBold)
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

  // Adjust Funds Dialog (Deposit or Withdraw)
  if (goalToAdjust != null) {
    val goal = goalToAdjust!!
    AlertDialog(
      onDismissRequest = { goalToAdjust = null },
      title = { Text(if (isDeposit) "Add to ${goal.title}" else "Withdraw from ${goal.title}") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Current saved: $currencySymbol ${String.format(Locale.US, "%,.2f", goal.currentAmount)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          OutlinedTextField(
            value = adjustDelta,
            onValueChange = { adjustDelta = it },
            label = { Text("Amount ($currencySymbol)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val amount = adjustDelta.toDoubleOrNull() ?: 0.0
            if (amount > 0) {
              val delta = if (isDeposit) amount else -amount
              onUpdateSavings(goal, delta)
              goalToAdjust = null
            }
          },
          enabled = (adjustDelta.toDoubleOrNull() ?: 0.0) > 0,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isDeposit) IncomeGreen else ExpenseRed
          )
        ) {
          Text(if (isDeposit) "Deposit" else "Withdraw")
        }
      },
      dismissButton = {
        TextButton(onClick = { goalToAdjust = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // Create Goal Dialog
  if (showAddDialog) {
    var title by remember { mutableStateOf("") }
    var targetInput by remember { mutableStateOf("") }
    var initialInput by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = { Text("New Savings Goal") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Goal Name (e.g. Emergency Fund)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          OutlinedTextField(
            value = targetInput,
            onValueChange = { targetInput = it },
            label = { Text("Target Amount ($currencySymbol)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          OutlinedTextField(
            value = initialInput,
            onValueChange = { initialInput = it },
            label = { Text("Initial Saved Amount (Optional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes / Purpose (Optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val target = targetInput.toDoubleOrNull() ?: 0.0
            val initial = initialInput.toDoubleOrNull() ?: 0.0
            if (title.isNotBlank() && target > 0) {
              onAddGoal(title.trim(), target, initial, notes.trim())
              showAddDialog = false
            }
          },
          enabled = title.isNotBlank() && (targetInput.toDoubleOrNull() ?: 0.0) > 0
        ) {
          Text("Create Goal")
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
