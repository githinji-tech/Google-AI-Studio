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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CategoryHelper
import com.example.data.model.TransactionEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryPromptDialog(
  transaction: TransactionEntity,
  currencySymbol: String = "KSh",
  onDismiss: () -> Unit,
  onSaveCategory: (TransactionEntity, String, String) -> Unit // (tx, category, notes)
) {
  var selectedCategory by remember {
    mutableStateOf(
      if (transaction.category.equals("Uncategorized", ignoreCase = true)) "Entertainment" else transaction.category
    )
  }
  var notes by remember { mutableStateOf(transaction.notes) }

  val isIncome = transaction.isIncome
  val categories = if (isIncome) {
    CategoryHelper.INCOME_CATEGORIES.filter { it.name != "Other" } + CategoryHelper.INCOME_CATEGORIES.filter { it.name == "Other" }
  } else {
    CategoryHelper.EXPENSE_CATEGORIES.filter { it.name != "Uncategorized" }
  }

  val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.US)
  val formattedDate = dateFormat.format(Date(transaction.date))

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .clip(RoundedCornerShape(24.dp))
        .testTag("category_prompt_dialog"),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Top Header
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
                .background(WarningAmber.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.HelpOutline,
                contentDescription = null,
                tint = WarningAmber,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Choose Category",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "No category keyword found",
                style = MaterialTheme.typography.labelSmall,
                color = WarningAmber
              )
            }
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Transaction Summary Card
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = transaction.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "$formattedDate • ${transaction.paymentMethod}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Text(
              text = "${if (isIncome) "+" else "-"}$currencySymbol ${String.format(Locale.US, "%,.2f", transaction.amount)}",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = if (isIncome) IncomeGreen else ExpenseRed
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Explanation text
        Text(
          text = "Nothing in this transaction tells us what was purchased (e.g. Google Play Store can be TikTok coins, games, subscriptions, or apps). What was this for?",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "SELECT CATEGORY",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
          color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Categories Grid/Flow
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          categories.forEach { cat ->
            val isSelected = selectedCategory.equals(cat.name, ignoreCase = true)
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { selectedCategory = cat.name }
                .testTag("prompt_category_${cat.name.lowercase().replace(" ", "_")}"),
              color = if (isSelected) cat.color.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
              shape = RoundedCornerShape(10.dp),
              border = androidx.compose.foundation.BorderStroke(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) cat.color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
              )
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = cat.icon,
                  contentDescription = null,
                  tint = if (isSelected) cat.color else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = cat.name,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                  ),
                  color = if (isSelected) cat.color else MaterialTheme.colorScheme.onSurface
                )
                if (isSelected) {
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = cat.color,
                    modifier = Modifier.size(14.dp)
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Optional Note (e.g. TikTok coins)
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Add Note (e.g., TikTok coins, gems, course)") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("category_prompt_note_input"),
          shape = RoundedCornerShape(12.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismiss) {
            Text("Skip for Now")
          }

          Spacer(modifier = Modifier.width(8.dp))

          Button(
            onClick = {
              onSaveCategory(transaction, selectedCategory, notes)
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("save_categorized_transaction_button")
          ) {
            Text("Save Category", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
