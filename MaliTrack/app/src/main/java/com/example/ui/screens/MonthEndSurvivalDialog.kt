package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.model.DailySpendMetrics
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WarningAmber
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthEndSurvivalSheet(
  metrics: DailySpendMetrics,
  currencySymbol: String,
  currentCustomAllowance: Double,
  onSaveCustomAllowance: (Double) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var customMoneyInput by remember {
    mutableStateOf(
      if (currentCustomAllowance > 0) currentCustomAllowance.toInt().toString()
      else if (metrics.totalAvailable > 0) metrics.totalAvailable.toInt().toString()
      else "7500"
    )
  }

  val activeMoney = customMoneyInput.toDoubleOrNull() ?: 0.0
  val uncommitted = (activeMoney - metrics.upcomingCommitments).coerceAtLeast(0.0)
  val daysRemaining = metrics.daysRemaining.coerceAtLeast(1)
  val recommendedDaily = if (daysRemaining > 0) uncommitted / daysRemaining else 0.0

  // Burn rate calculation
  val burnPace = if (metrics.threeDayBurnRate > 0) metrics.threeDayBurnRate else recommendedDaily
  val runwayDays = if (burnPace > 0) (uncommitted / burnPace).toInt() else daysRemaining
  val isRunwayShort = runwayDays < daysRemaining

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = Modifier.testTag("survival_sheet")
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
              .background(IncomeGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Speed,
              contentDescription = null,
              tint = IncomeGreen,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Survive Until Month-End",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Student Runway & Pace Planner",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Custom Money Pool Input
      Text(
        text = "Your Total Money Available for the Month",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(4.dp))
      OutlinedTextField(
        value = customMoneyInput,
        onValueChange = { customMoneyInput = it },
        placeholder = { Text("e.g. 7500") },
        prefix = { Text("$currencySymbol ") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("survival_money_input"),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
      )

      Spacer(modifier = Modifier.height(16.dp))

      // 4 Key Survival Metrics Grid
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Metric 1: Recommended Daily Spending
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "RECOMMENDED DAILY",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "$currencySymbol ${String.format(Locale.US, "%,.0f", recommendedDaily)}",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
              text = "per day to survive",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
          }
        }

        // Metric 2: Days Remaining
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "DAYS REMAINING",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "$daysRemaining days",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "until next allowance",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Metric 3 & 4 Row: Locked Commitments & Uncommitted
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Locked Recurring Bills
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = WarningAmber,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "LOCKED BILLS",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "$currencySymbol ${String.format(Locale.US, "%,.0f", metrics.upcomingCommitments)}",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Rent, Wi-Fi, etc.",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Uncommitted Cash
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "FREE TO SPEND",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "$currencySymbol ${String.format(Locale.US, "%,.0f", uncommitted)}",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = IncomeGreen
            )
            Text(
              text = "After all fixed bills",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Runway Forecast Callout
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isRunwayShort) ExpenseRed.copy(alpha = 0.12f) else IncomeGreen.copy(alpha = 0.12f)
        ),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (isRunwayShort) ExpenseRed.copy(alpha = 0.35f) else IncomeGreen.copy(alpha = 0.35f)
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (isRunwayShort) Icons.Default.Warning else Icons.Default.Info,
              contentDescription = null,
              tint = if (isRunwayShort) ExpenseRed else IncomeGreen,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isRunwayShort) "Pace Warning: Runway Deficit" else "Healthy Spending Pace",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = if (isRunwayShort) ExpenseRed else IncomeGreen
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          if (isRunwayShort) {
            Text(
              text = "At your current 3-day spend pace of $currencySymbol ${String.format(Locale.US, "%,.0f", burnPace)}/day, your money will run out in $runwayDays days (${daysRemaining - runwayDays} days before month-end!).",
              style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "💡 Strategy: Slow down to $currencySymbol ${String.format(Locale.US, "%,.0f", recommendedDaily)}/day starting tomorrow to survive comfortably.",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurface
            )
          } else {
            Text(
              text = "Great job! At your current spending rate, your money will last comfortably through the remaining $daysRemaining days with a surplus.",
              style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Button(
        onClick = {
          onSaveCustomAllowance(activeMoney)
          onDismiss()
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("save_survival_budget_btn"),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text(
          text = "Save as My Monthly Allowance Target",
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
      }
    }
  }
}
