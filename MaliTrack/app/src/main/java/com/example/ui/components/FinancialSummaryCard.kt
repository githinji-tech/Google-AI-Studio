package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BudgetEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WarningAmber
import java.util.Locale

@Composable
fun MonthSelectorHeader(
  monthName: String,
  onPreviousMonth: () -> Unit,
  onNextMonth: () -> Unit,
  onCurrentMonth: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    IconButton(
      onClick = onPreviousMonth,
      modifier = Modifier.testTag("prev_month_button")
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "Previous Month",
        tint = MaterialTheme.colorScheme.onSurface
      )
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Text(
        text = monthName,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp
        ),
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.width(4.dp))
      IconButton(
        onClick = onCurrentMonth,
        modifier = Modifier
          .size(32.dp)
          .testTag("current_month_button")
      ) {
        Icon(
          imageVector = Icons.Default.Today,
          contentDescription = "Current Month",
          modifier = Modifier.size(18.dp),
          tint = MaterialTheme.colorScheme.primary
        )
      }
    }

    IconButton(
      onClick = onNextMonth,
      modifier = Modifier.testTag("next_month_button")
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = "Next Month",
        tint = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

@Composable
fun FinancialSummaryCard(
  totalIncome: Double,
  totalExpense: Double,
  currencySymbol: String,
  overallBudget: BudgetEntity?,
  onSetBudgetClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val netBalance = totalIncome - totalExpense

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    ) {
      // Net Balance Header
      Text(
        text = "NET MONTHLY BALANCE",
        style = MaterialTheme.typography.labelSmall.copy(
          letterSpacing = 1.2.sp,
          fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "$currencySymbol ${String.format(Locale.US, "%,.2f", netBalance)}",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.ExtraBold
        ),
        color = if (netBalance >= 0) MaterialTheme.colorScheme.onPrimaryContainer else ExpenseRed
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Income & Expense Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Income Box
        Row(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(IncomeGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ArrowDownward,
              contentDescription = "Income",
              tint = IncomeGreen,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "Income",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "$currencySymbol ${String.format(Locale.US, "%,.0f", totalIncome)}",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              color = IncomeGreen
            )
          }
        }

        // Expense Box
        Row(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(ExpenseRed.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ArrowUpward,
              contentDescription = "Expenses",
              tint = ExpenseRed,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "Expenses",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "$currencySymbol ${String.format(Locale.US, "%,.0f", totalExpense)}",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              color = ExpenseRed
            )
          }
        }
      }

      // Budget Status Bar
      if (overallBudget != null && overallBudget.limitAmount > 0) {
        val budgetLimit = overallBudget.limitAmount
        val ratio = (totalExpense / budgetLimit).toFloat().coerceIn(0f, 1f)
        val isOver = totalExpense > budgetLimit
        val isNear = totalExpense >= budgetLimit * 0.8 && !isOver

        Spacer(modifier = Modifier.height(14.dp))

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .padding(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              if (isOver || isNear) {
                Icon(
                  imageVector = Icons.Default.Warning,
                  contentDescription = null,
                  tint = if (isOver) ExpenseRed else WarningAmber,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
              }
              Text(
                text = if (isOver) "Budget Exceeded!" else if (isNear) "Near Budget Limit" else "Monthly Budget",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isOver) ExpenseRed else if (isNear) WarningAmber else MaterialTheme.colorScheme.onSurface
              )
            }
            Text(
              text = "$currencySymbol ${String.format(Locale.US, "%,.0f", totalExpense)} / ${String.format(Locale.US, "%,.0f", budgetLimit)}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = if (isOver) ExpenseRed else if (isNear) WarningAmber else IncomeGreen,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
          )
        }
      } else {
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
          onClick = onSetBudgetClick,
          modifier = Modifier
            .align(Alignment.End)
            .testTag("set_monthly_budget_btn")
        ) {
          Text(
            text = "+ Set Monthly Budget Limit",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }
    }
  }
}
