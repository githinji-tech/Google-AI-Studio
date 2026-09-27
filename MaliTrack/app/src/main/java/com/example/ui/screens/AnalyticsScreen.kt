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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SpendingLeak
import com.example.data.model.TransactionEntity
import com.example.ui.components.CategorySpendingBreakdown
import com.example.ui.components.DailySpendingBarChart
import com.example.ui.components.LeakDetectorCard
import com.example.ui.components.MonthSelectorHeader
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
  monthName: String,
  transactions: List<TransactionEntity>,
  calendar: Calendar,
  currencySymbol: String,
  spendingLeaks: List<SpendingLeak> = emptyList(),
  isPremium: Boolean = false,
  onOpenUpgrade: () -> Unit = {},
  onPreviousMonth: () -> Unit,
  onNextMonth: () -> Unit,
  onCurrentMonth: () -> Unit,
  modifier: Modifier = Modifier
) {
  val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount }
  val totalExpense = transactions.filter { it.isExpense }.sumOf { it.amount }
  val net = totalIncome - totalExpense

  val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
  val avgDailySpend = if (daysInMonth > 0) totalExpense / daysInMonth else 0.0

  val savingsRate = if (totalIncome > 0) ((net / totalIncome) * 100).coerceAtLeast(0.0) else 0.0

  val topCategory = transactions.filter { it.isExpense }
    .groupBy { it.category }
    .mapValues { it.value.sumOf { tx -> tx.amount } }
    .maxByOrNull { it.value }

  Column(modifier = modifier.fillMaxSize()) {
    TopAppBar(
      title = {
        Text(
          text = "Spending & Insights",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
      }
    )

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        MonthSelectorHeader(
          monthName = monthName,
          onPreviousMonth = onPreviousMonth,
          onNextMonth = onNextMonth,
          onCurrentMonth = onCurrentMonth
        )
      }

      // 3 Stat Cards Row
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StatCard(
            title = "Savings Rate",
            value = "${String.format(Locale.US, "%.1f", savingsRate)}%",
            icon = Icons.Default.Savings,
            tint = IncomeGreen,
            modifier = Modifier.weight(1f)
          )

          StatCard(
            title = "Avg / Day",
            value = "$currencySymbol ${String.format(Locale.US, "%,.0f", avgDailySpend)}",
            icon = Icons.Default.Speed,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
          )

          StatCard(
            title = "Top Spend",
            value = topCategory?.key ?: "None",
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            tint = ExpenseRed,
            modifier = Modifier.weight(1f)
          )
        }
      }

      // Leak Detector Card
      if (spendingLeaks.isNotEmpty()) {
        item {
          LeakDetectorCard(
            leaks = spendingLeaks,
            currencySymbol = currencySymbol,
            isPremium = isPremium,
            onOpenUpgrade = onOpenUpgrade
          )
        }
      }

      // Spending by category breakdown
      item {
        CategorySpendingBreakdown(
          transactions = transactions,
          currencySymbol = currencySymbol
        )
      }

      // Daily spending trend chart
      item {
        DailySpendingBarChart(
          transactions = transactions,
          calendar = calendar,
          currencySymbol = currencySymbol
        )
      }

      item {
        Spacer(modifier = Modifier.height(48.dp))
      }
    }
  }
}

@Composable
private fun StatCard(
  title: String,
  value: String,
  icon: ImageVector,
  tint: androidx.compose.ui.graphics.Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(tint.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = tint,
          modifier = Modifier.size(16.dp)
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1
      )
    }
  }
}
