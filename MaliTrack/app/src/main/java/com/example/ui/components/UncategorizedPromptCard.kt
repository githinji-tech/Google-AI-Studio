package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryHelper
import com.example.data.model.TransactionEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UncategorizedPromptCard(
  uncategorizedList: List<TransactionEntity>,
  currencySymbol: String = "KSh",
  onQuickCategorize: (TransactionEntity, String) -> Unit,
  onOpenDetailedPrompt: (TransactionEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  if (uncategorizedList.isEmpty()) return

  val currentTx = uncategorizedList.first()
  val count = uncategorizedList.size
  val dateFormat = SimpleDateFormat("dd MMM", Locale.US)
  val formattedDate = dateFormat.format(Date(currentTx.date))

  // Quick categories suitable for student purchases without keywords (like TikTok coins on Google Play)
  val quickCategories = listOf("Entertainment", "Shopping", "Bills & Utilities", "Education", "Food & Dining")

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .testTag("uncategorized_prompt_card"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.12f)),
    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.4f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
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
              .size(28.dp)
              .clip(CircleShape)
              .background(WarningAmber.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.HelpOutline,
              contentDescription = null,
              tint = WarningAmber,
              modifier = Modifier.size(16.dp)
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Text(
            text = if (count == 1) "1 Transaction Needs Category" else "$count Transactions Need Category",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = WarningAmber.copy(alpha = 0.2f),
          modifier = Modifier.clickable { onOpenDetailedPrompt(currentTx) }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Categorize",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Nothing in '${currentTx.title}' tells what you bought (e.g. TikTok coins on Google Play, transfer). What was this for?",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Current transaction snippet
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = currentTx.title,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "$formattedDate • ${currentTx.paymentMethod}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Text(
            text = "${if (currentTx.isIncome) "+" else "-"}$currencySymbol ${String.format(Locale.US, "%,.2f", currentTx.amount)}",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = if (currentTx.isIncome) IncomeGreen else ExpenseRed
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Quick-pick category chips row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        quickCategories.forEach { catName ->
          val meta = CategoryHelper.getCategoryMeta(catName)
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable { onQuickCategorize(currentTx, catName) }
              .testTag("quick_cat_${catName.lowercase().replace(" ", "_")}"),
            color = meta.color.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, meta.color.copy(alpha = 0.4f))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = meta.icon,
                contentDescription = null,
                tint = meta.color,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (catName == "Entertainment") "Entertainment (TikTok/Games)" else catName,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = meta.color
              )
            }
          }
        }

        // More button
        Surface(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onOpenDetailedPrompt(currentTx) },
          color = MaterialTheme.colorScheme.surfaceVariant,
          shape = RoundedCornerShape(8.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.MoreHoriz,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "More",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}
