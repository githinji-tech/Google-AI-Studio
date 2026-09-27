package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailySpendMetrics
import com.example.ui.theme.AirtelRed
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WarningAmber
import java.util.Locale

@Composable
fun SafeDailySpendHeroCard(
  metrics: DailySpendMetrics,
  currencySymbol: String,
  isPremium: Boolean,
  affordChecksRemaining: Int = 3,
  onOpenAffordability: () -> Unit,
  onOpenSurvivalGuide: () -> Unit,
  onOpenUpgrade: () -> Unit,
  modifier: Modifier = Modifier
) {
  // Gradient background highlighting positive control
  val backgroundBrush = if (metrics.isOverToday) {
    Brush.verticalGradient(
      colors = listOf(
        Color(0xFF2A1616),
        Color(0xFF1E1414)
      )
    )
  } else {
    Brush.verticalGradient(
      colors = listOf(
        Color(0xFF0F291E),
        Color(0xFF0A1D15)
      )
    )
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(backgroundBrush)
        .border(
          width = 1.dp,
          color = if (metrics.isOverToday) ExpenseRed.copy(alpha = 0.35f) else IncomeGreen.copy(alpha = 0.35f),
          shape = RoundedCornerShape(24.dp)
        )
        .padding(20.dp)
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Top Row: Paradigm Statement + Days Remaining Badge
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (metrics.isOverToday) ExpenseRed else IncomeGreen)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "SAFE DAILY SPEND",
              style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
              ),
              color = Color.White.copy(alpha = 0.75f)
            )
          }

          // Days Remaining Badge
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.12f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${metrics.daysRemaining} days left",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 11.sp
                ),
                color = Color.White.copy(alpha = 0.9f)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Number: Recommended Daily Allowance
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Bottom
        ) {
          Column {
            Text(
              text = "$currencySymbol ${String.format(Locale.US, "%,.0f", metrics.recommendedDailyAllowance)}",
              style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
              ),
              color = Color.White
            )
            Text(
              text = "Daily allowance until month-end",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.7f)
            )
          }

          // Today's Spent Counter
          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "Spent Today",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.6f)
            )
            Text(
              text = "$currencySymbol ${String.format(Locale.US, "%,.0f", metrics.todaySpent)}",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = if (metrics.isOverToday) ExpenseRed else Color.White
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Daily Progress Bar (Today's Spend / Daily Allowance)
        val dailyProgress = if (metrics.recommendedDailyAllowance > 0) {
          (metrics.todaySpent / metrics.recommendedDailyAllowance).toFloat().coerceIn(0f, 1f)
        } else 0f

        LinearProgressIndicator(
          progress = { dailyProgress },
          modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(RoundedCornerShape(3.5.dp)),
          color = if (metrics.isOverToday) ExpenseRed else IncomeGreen,
          trackColor = Color.White.copy(alpha = 0.15f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Dynamic Status Callout (The core student reassurance/alert)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
              if (metrics.isOverToday) ExpenseRed.copy(alpha = 0.18f)
              else IncomeGreen.copy(alpha = 0.18f)
            )
            .padding(10.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (metrics.isOverToday) Icons.Default.Warning else Icons.Default.CheckCircle,
              contentDescription = null,
              tint = if (metrics.isOverToday) ExpenseRed else IncomeGreen,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              if (metrics.isOverToday) {
                Text(
                  text = "Over today's target by $currencySymbol ${String.format(Locale.US, "%,.0f", metrics.overAmount)}",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = Color.White
                )
                Text(
                  text = "Tomorrow's safe spend adjusted down to $currencySymbol ${String.format(Locale.US, "%,.0f", metrics.adjustedTomorrowDaily)}/day.",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                  color = Color.White.copy(alpha = 0.8f)
                )
              } else {
                Text(
                  text = "$currencySymbol ${String.format(Locale.US, "%,.0f", metrics.safeTodayRemaining)} safe to spend today",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = Color.White
                )
                Text(
                  text = "Money remaining: $currencySymbol ${String.format(Locale.US, "%,.0f", metrics.uncommittedRemaining)} uncommitted.",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                  color = Color.White.copy(alpha = 0.8f)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Student Quick-Action Decision Chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // "Can I Afford This?" Button
          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .clickable { onOpenAffordability() }
              .testTag("can_i_afford_btn"),
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.15f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.HelpOutline,
                contentDescription = null,
                tint = WarningAmber,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isPremium) "Can I Afford This?" else "Can I Afford? ($affordChecksRemaining/3)",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp
                ),
                color = Color.White
              )
            }
          }

          // "Survive Until Month-End" Button
          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .clickable { onOpenSurvivalGuide() }
              .testTag("survival_guide_btn"),
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.15f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = IncomeGreen,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Month-End Runway",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp
                ),
                color = Color.White
              )
            }
          }
        }

        // Student Pro Badge / Upgrade teaser
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenUpgrade() }
            .padding(vertical = 2.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (isPremium) Icons.Default.Shield else Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = if (isPremium) IncomeGreen else WarningAmber,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = if (isPremium) "MaliTrack Student Pro Active" else "Student Pro: 25 bob/week or 100 bob/mo",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
              ),
              color = if (isPremium) IncomeGreen else WarningAmber
            )
          }

          Text(
            text = if (isPremium) "Benefits" else "Save 10x More →",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            ),
            color = Color.White.copy(alpha = 0.8f)
          )
        }
      }
    }
  }
}
