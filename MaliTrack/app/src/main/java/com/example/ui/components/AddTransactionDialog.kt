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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CategoryHelper
import com.example.data.model.TransactionEntity
import com.example.ui.theme.AirtelRed
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.MpesaGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTransactionDialog(
  initialType: String = "EXPENSE",
  currencySymbol: String = "KSh",
  onDismiss: () -> Unit,
  onSave: (
    type: String,
    amount: Double,
    title: String,
    category: String,
    date: Long,
    paymentMethod: String,
    referenceCode: String,
    notes: String
  ) -> Unit
) {
  var type by remember { mutableStateOf(initialType) }
  var amountText by remember { mutableStateOf("") }
  var title by remember { mutableStateOf("") }
  val availableCategories = if (type == "EXPENSE") CategoryHelper.EXPENSE_CATEGORIES else CategoryHelper.INCOME_CATEGORIES
  var selectedCategory by remember(type) { mutableStateOf(availableCategories.first().name) }
  var selectedMethod by remember { mutableStateOf("M-Pesa") }
  var referenceCode by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }

  val paymentMethods = listOf("M-Pesa", "Airtel Money", "Cash", "Card", "Bank Transfer")

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .clip(RoundedCornerShape(24.dp)),
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
          Text(
            text = if (type == "EXPENSE") "Add Expense" else "Add Income",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_add_tx_dialog")) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Type Selector Tabs (Expense vs Income)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (type == "EXPENSE") ExpenseRed else Color.Transparent)
              .clickable { type = "EXPENSE" }
              .padding(vertical = 10.dp)
              .testTag("select_expense_tab"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Expense",
              fontWeight = FontWeight.Bold,
              color = if (type == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (type == "INCOME") IncomeGreen else Color.Transparent)
              .clickable { type = "INCOME" }
              .padding(vertical = 10.dp)
              .testTag("select_income_tab"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Income",
              fontWeight = FontWeight.Bold,
              color = if (type == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Amount Input
        OutlinedTextField(
          value = amountText,
          onValueChange = { amountText = it },
          label = { Text("Amount ($currencySymbol)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("amount_input"),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Title / Description
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text(if (type == "EXPENSE") "Merchant / Description (e.g. Naivas)" else "Source (e.g. Salary, Client)") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("title_input"),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Selection
        Text(
          text = "Category",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          availableCategories.forEach { meta ->
            val isSelected = selectedCategory == meta.name
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) meta.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                  width = if (isSelected) 1.5.dp else 0.dp,
                  color = if (isSelected) meta.color else Color.Transparent,
                  shape = RoundedCornerShape(10.dp)
                )
                .clickable { selectedCategory = meta.name }
                .padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = meta.icon,
                contentDescription = meta.name,
                tint = meta.color,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = meta.name,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Payment Method
        Text(
          text = "Payment Method",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          paymentMethods.forEach { method ->
            val isSelected = selectedMethod == method
            val brandColor = when (method) {
              "M-Pesa" -> MpesaGreen
              "Airtel Money" -> AirtelRed
              else -> MaterialTheme.colorScheme.primary
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) brandColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                  width = if (isSelected) 1.5.dp else 0.dp,
                  color = if (isSelected) brandColor else Color.Transparent,
                  shape = RoundedCornerShape(8.dp)
                )
                .clickable { selectedMethod = method }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = method,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isSelected) brandColor else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Optional Reference Code
        OutlinedTextField(
          value = referenceCode,
          onValueChange = { referenceCode = it },
          label = { Text("Reference / Txn Code (Optional)") },
          placeholder = { Text("e.g. QA12BC34DE") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Submit Button
        val isValid = (amountText.toDoubleOrNull() ?: 0.0) > 0 && title.isNotBlank()

        Button(
          onClick = {
            val amt = amountText.toDoubleOrNull() ?: 0.0
            onSave(
              type,
              amt,
              title.trim(),
              selectedCategory,
              System.currentTimeMillis(),
              selectedMethod,
              referenceCode.trim(),
              notes.trim()
            )
          },
          enabled = isValid,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("save_transaction_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (type == "EXPENSE") ExpenseRed else IncomeGreen
          )
        ) {
          Text(
            text = if (type == "EXPENSE") "Save Expense" else "Save Income",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White
          )
        }
      }
    }
  }
}
