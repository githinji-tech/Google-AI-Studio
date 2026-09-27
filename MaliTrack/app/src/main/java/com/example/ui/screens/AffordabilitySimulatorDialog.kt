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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.data.model.AffordVerdict
import com.example.data.model.AffordabilitySimulation
import com.example.data.model.DailySpendMetrics
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WarningAmber
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AffordabilitySimulatorSheet(
  metrics: DailySpendMetrics,
  currencySymbol: String,
  isPremium: Boolean,
  affordChecksRemaining: Int,
  onDismiss: () -> Unit,
  onLogExpense: (itemName: String, amount: Double) -> Unit,
  onUseAffordCheck: () -> Boolean = { true },
  onOpenUpgrade: () -> Unit,
  onSimulate: (cost: Double, itemName: String, metrics: DailySpendMetrics) -> AffordabilitySimulation
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var itemName by remember { mutableStateOf("") }
  var costInput by remember { mutableStateOf("") }
  var hasCalculatedThisSession by remember { mutableStateOf(false) }
  var isCalculated by remember { mutableStateOf(false) }

  val cost = costInput.toDoubleOrNull() ?: 0.0

  val samplePicks = listOf(
    "Shoes" to 800.0,
    "Fast Food" to 450.0,
    "Concert" to 1500.0,
    "New Shirt" to 950.0,
    "Hostel Wi-Fi" to 500.0,
    "Weekend Trip" to 2200.0
  )

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = Modifier.testTag("affordability_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .padding(bottom = 36.dp)
        .verticalScroll(rememberScrollState())
    ) {
      // Header
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
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.HelpOutline,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Can I Afford This?",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Instant purchase impact simulator",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Monthly Quota Status Banner
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = when {
          isPremium -> IncomeGreen.copy(alpha = 0.12f)
          affordChecksRemaining > 0 -> WarningAmber.copy(alpha = 0.15f)
          else -> ExpenseRed.copy(alpha = 0.15f)
        }
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = when {
                isPremium -> Icons.Default.Shield
                affordChecksRemaining > 0 -> Icons.Default.AutoAwesome
                else -> Icons.Default.Lock
              },
              contentDescription = null,
              tint = when {
                isPremium -> IncomeGreen
                affordChecksRemaining > 0 -> WarningAmber
                else -> ExpenseRed
              },
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = when {
                isPremium -> "Student Pro • Unlimited Simulations"
                affordChecksRemaining > 0 -> "Free Tier • $affordChecksRemaining of 3 uses left this month"
                else -> "Monthly Limit Reached • 0 of 3 uses left"
              },
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = when {
                isPremium -> IncomeGreen
                affordChecksRemaining > 0 -> MaterialTheme.colorScheme.onSurface
                else -> ExpenseRed
              }
            )
          }

          if (!isPremium) {
            Text(
              text = if (affordChecksRemaining > 0) "Get Unlimited" else "Unlock Pro",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.clickable { onOpenUpgrade() }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Current Baseline Pill
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "CURRENT SAFE ALLOWANCE",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "$currencySymbol ${String.format(Locale.US, "%,.0f", metrics.recommendedDailyAllowance)}/day",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
              color = IncomeGreen
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "REMAINING POOL",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "$currencySymbol ${String.format(Locale.US, "%,.0f", metrics.uncommittedRemaining)}",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Quick student suggestion chips
      Text(
        text = "Quick student items:",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(6.dp))
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        samplePicks.forEach { (name, amt) ->
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable {
                itemName = name
                costInput = amt.toInt().toString()
              },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
          ) {
            Text(
              text = "$name ($currencySymbol ${amt.toInt()})",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Input: What do you want to buy?
      OutlinedTextField(
        value = itemName,
        onValueChange = { itemName = it },
        label = { Text("What do you want to buy?") },
        placeholder = { Text("e.g. Shoes, Pizza, Jacket") },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("afford_item_input"),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Input: How much does it cost?
      OutlinedTextField(
        value = costInput,
        onValueChange = { costInput = it },
        label = { Text("How much does it cost? ($currencySymbol)") },
        placeholder = { Text("e.g. 800") },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("afford_cost_input"),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Action Button or Limit Exhausted Card
      if (!isPremium && affordChecksRemaining <= 0 && !hasCalculatedThisSession) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("afford_quota_exhausted_card"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
          border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f))
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Lock, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "3 of 3 Monthly Free Uses Completed",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Freemium accounts get 3 'Can I Afford This?' queries each month. To run unlimited purchase impact checks without waiting for next month, upgrade to Student Pro for only 25 bob/week or 100 bob/month.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = onOpenUpgrade,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("upgrade_from_afford_limit_btn"),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Upgrade to Student Pro (From 25 bob)")
            }
          }
        }
      } else {
        Button(
          onClick = {
            val parsedCost = costInput.toDoubleOrNull() ?: 0.0
            if (parsedCost > 0) {
              if (!isPremium && !hasCalculatedThisSession) {
                onUseAffordCheck()
                hasCalculatedThisSession = true
              }
              isCalculated = true
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("simulate_impact_btn"),
          shape = RoundedCornerShape(12.dp),
          enabled = cost > 0
        ) {
          Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isPremium) {
              "Check Affordability Impact"
            } else if (affordChecksRemaining > 0) {
              "Check Affordability ($affordChecksRemaining of 3 left this month)"
            } else {
              "Check Affordability"
            },
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Dynamic Simulation Result Card
      if (cost > 0 && (isCalculated || isPremium || hasCalculatedThisSession)) {
        val simulation = onSimulate(
          cost,
          if (itemName.isNotBlank()) itemName else "Simulated Purchase",
          metrics
        )
        val daysRemaining = simulation.remainingDays
        val newDaily = simulation.newDailyAllowance
        val drop = simulation.dailyDrop

        val verdict: AffordVerdict = simulation.verdict
        val verdictTitle: String
        val verdictColor: Color
        val verdictDesc: String = simulation.explanation

        when (verdict) {
          AffordVerdict.DANGEROUS -> {
            verdictTitle = if (cost > metrics.uncommittedRemaining) "CANNOT AFFORD" else "HIGH RISK OF RUNNING BROKE"
            verdictColor = ExpenseRed
          }
          AffordVerdict.TIGHT_SQUEEZE -> {
            verdictTitle = "TIGHT SQUEEZE"
            verdictColor = WarningAmber
          }
          AffordVerdict.SAFE -> {
            verdictTitle = "SAFE TO BUY"
            verdictColor = IncomeGreen
          }
        }

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = verdictColor.copy(alpha = 0.12f)),
          border = androidx.compose.foundation.BorderStroke(1.dp, verdictColor.copy(alpha = 0.4f))
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (verdict == AffordVerdict.SAFE) Icons.Default.CheckCircle else Icons.Default.Warning,
                  contentDescription = null,
                  tint = verdictColor,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = verdictTitle,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                  color = verdictColor
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = verdictDesc,
              style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
              color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Comparison row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(text = "Now", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                  text = "$currencySymbol ${String.format(Locale.US, "%,.0f", metrics.recommendedDailyAllowance)}/d",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Daily Drop", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                  text = "-$currencySymbol ${String.format(Locale.US, "%,.0f", drop)}",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                  color = ExpenseRed
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(text = "After Purchase", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                  text = "$currencySymbol ${String.format(Locale.US, "%,.0f", newDaily)}/d",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                  color = if (newDaily < 150) ExpenseRed else IncomeGreen
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Log directly button if user decides to buy
            Button(
              onClick = {
                val title = if (itemName.isNotBlank()) itemName else "Simulated Purchase"
                onLogExpense(title, cost)
                onDismiss()
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("log_afford_expense_btn"),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (verdict == AffordVerdict.SAFE) IncomeGreen else MaterialTheme.colorScheme.primary
              ),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text(
                text = "Buy & Log as Expense ($currencySymbol ${String.format(Locale.US, "%,.0f", cost)})",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Freemium / Student Pro info note
      if (!isPremium) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenUpgrade() },
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = WarningAmber,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Unlimited Simulations with Student Pro",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Get unlimited 'Can I Afford This?' queries for only 25 bob/week or 100 bob/month.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }
  }
}
