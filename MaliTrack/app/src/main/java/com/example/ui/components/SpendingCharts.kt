package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryHelper
import com.example.data.model.TransactionEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.util.Calendar
import java.util.Locale

@Composable
fun CategorySpendingBreakdown(
  transactions: List<TransactionEntity>,
  currencySymbol: String,
  modifier: Modifier = Modifier
) {
  val expenseTransactions = transactions.filter { it.isExpense }
  val totalExpense = expenseTransactions.sumOf { it.amount }

  val categoryTotals = expenseTransactions
    .groupBy { it.category }
    .mapValues { entry -> entry.value.sumOf { it.amount } }
    .toList()
    .sortedByDescending { it.second }

  Card(
    modifier = modifier
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
        Text(
          text = "Category Breakdown",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "${categoryTotals.size} categories",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (totalExpense <= 0 || categoryTotals.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No expenses recorded for this month",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        // Multi-segment horizontal bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
        ) {
          categoryTotals.forEach { (catName, amount) ->
            val ratio = (amount / totalExpense).toFloat()
            val meta = CategoryHelper.getCategoryMeta(catName)
            if (ratio > 0.01f) {
              Box(
                modifier = Modifier
                  .weight(ratio.coerceAtLeast(0.01f))
                  .fillMaxHeight()
                  .background(meta.color)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category items list
        categoryTotals.forEach { (catName, amount) ->
          val meta = CategoryHelper.getCategoryMeta(catName)
          val percentage = (amount / totalExpense) * 100

          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 6.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(meta.color.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = meta.icon,
                    contentDescription = catName,
                    tint = meta.color,
                    modifier = Modifier.size(16.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = catName,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "$currencySymbol ${String.format(Locale.US, "%,.2f", amount)}",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "${String.format(Locale.US, "%.1f", percentage)}%",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
              progress = { (percentage / 100f).toFloat().coerceIn(0f, 1f) },
              modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
              color = meta.color,
              trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
          }
        }
      }
    }
  }
}

@Composable
fun DailySpendingBarChart(
  transactions: List<TransactionEntity>,
  calendar: Calendar,
  currencySymbol: String,
  modifier: Modifier = Modifier
) {
  val expenseTxs = transactions.filter { it.isExpense }
  val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

  // Map day (1..daysInMonth) to amount
  val dailySpend = mutableMapOf<Int, Double>()
  for (day in 1..daysInMonth) {
    dailySpend[day] = 0.0
  }
  for (tx in expenseTxs) {
    val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
    val day = txCal.get(Calendar.DAY_OF_MONTH)
    dailySpend[day] = (dailySpend[day] ?: 0.0) + tx.amount
  }

  val maxSpend = (dailySpend.values.maxOrNull() ?: 1.0).coerceAtLeast(100.0)
  val primaryColor = MaterialTheme.colorScheme.primary
  val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

  Card(
    modifier = modifier
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
        Text(
          text = "Daily Spending Trend",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Peak: $currencySymbol ${String.format(Locale.US, "%,.0f", maxSpend)}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
      ) {
        val totalWidth = size.width
        val totalHeight = size.height - 20.dp.toPx()
        val barSpacing = 2.dp.toPx()
        val barWidth = (totalWidth - (barSpacing * (daysInMonth - 1))) / daysInMonth

        for (day in 1..daysInMonth) {
          val spend = dailySpend[day] ?: 0.0
          val heightRatio = (spend / maxSpend).toFloat().coerceIn(0f, 1f)
          val barHeight = (totalHeight * heightRatio).coerceAtLeast(2.dp.toPx())

          val left = (day - 1) * (barWidth + barSpacing)
          val top = totalHeight - barHeight

          // Draw bar
          drawRoundRect(
            color = if (spend > 0) primaryColor else surfaceVariant.copy(alpha = 0.5f),
            topLeft = Offset(left, top),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
          )
        }
      }

      // X-Axis labels (1, 10, 20, end of month)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(text = "1st", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "10th", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "20th", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "${daysInMonth}th", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
}
