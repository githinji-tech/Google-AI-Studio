package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CategoryHelper
import com.example.data.model.TransactionEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditTransactionDialog(
  transaction: TransactionEntity,
  currencySymbol: String = "KSh",
  onDismiss: () -> Unit,
  onSave: (TransactionEntity) -> Unit,
  onDelete: () -> Unit
) {
  var title by remember { mutableStateOf(transaction.title) }
  var amountText by remember { mutableStateOf(if (transaction.amount % 1.0 == 0.0) transaction.amount.toInt().toString() else transaction.amount.toString()) }
  var selectedCategory by remember { mutableStateOf(transaction.category) }
  var selectedMethod by remember { mutableStateOf(transaction.paymentMethod) }
  var notes by remember { mutableStateOf(transaction.notes) }

  val isIncome = transaction.isIncome
  val categories = if (isIncome) CategoryHelper.INCOME_CATEGORIES else CategoryHelper.EXPENSE_CATEGORIES
  val paymentMethods = listOf("M-Pesa", "Airtel Money", "Cash", "Card", "Bank Transfer")

  val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
  val formattedDate = dateFormat.format(Date(transaction.date))

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .clip(RoundedCornerShape(24.dp))
        .testTag("edit_transaction_dialog"),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Top Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Edit Transaction",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = formattedDate,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title / Description
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Title / Counterparty") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("edit_tx_title_input"),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Amount
        OutlinedTextField(
          value = amountText,
          onValueChange = { amountText = it },
          label = { Text("Amount ($currencySymbol)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("edit_tx_amount_input"),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Picker Section
        Text(
          text = "Select Category:",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          categories.forEach { cat ->
            val isSelected = cat.name.equals(selectedCategory, ignoreCase = true)
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { selectedCategory = cat.name }
                .border(
                  width = if (isSelected) 2.dp else 1.dp,
                  color = if (isSelected) cat.color else MaterialTheme.colorScheme.outlineVariant,
                  shape = RoundedCornerShape(10.dp)
                ),
              color = if (isSelected) cat.color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              shape = RoundedCornerShape(10.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = cat.icon,
                  contentDescription = null,
                  tint = if (isSelected) cat.color else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = cat.name,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  ),
                  color = if (isSelected) cat.color else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Payment Method
        Text(
          text = "Payment Method:",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          paymentMethods.forEach { method ->
            val isSelected = method.equals(selectedMethod, ignoreCase = true)
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { selectedMethod = method },
              color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = method,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Notes
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Notes (e.g. TikTok coins, grocery list)") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          maxLines = 2
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Actions: Delete & Save
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = onDelete,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
            border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Delete")
          }

          Button(
            onClick = {
              val parsedAmount = amountText.toDoubleOrNull() ?: transaction.amount
              val updated = transaction.copy(
                title = title.trim().ifBlank { transaction.title },
                amount = parsedAmount,
                category = selectedCategory,
                paymentMethod = selectedMethod,
                notes = notes.trim()
              )
              onSave(updated)
            },
            modifier = Modifier.testTag("save_edited_tx_btn"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Save Changes", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
          }
        }
      }
    }
  }
}
