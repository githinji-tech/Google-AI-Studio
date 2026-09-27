package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ui.theme.AirtelRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumUpgradeSheet(
  isCurrentlyPremium: Boolean,
  currencySymbol: String,
  onDismiss: () -> Unit,
  onUnlockPremium: (plan: String) -> Unit,
  onTogglePremium: (Boolean) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()

  var selectedPlan by remember { mutableStateOf("MONTHLY_100") }
  var paymentMethod by remember { mutableStateOf("M-PESA") }
  var phoneNumber by remember { mutableStateOf("0712345678") }

  // Simulation state
  var isProcessingPayment by remember { mutableStateOf(false) }
  var paymentSuccessCode by remember { mutableStateOf<String?>(null) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = Modifier.testTag("premium_upgrade_sheet")
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
              .size(40.dp)
              .clip(CircleShape)
              .background(WarningAmber.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = WarningAmber,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "MaliTrack Student Pro",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
            )
            Text(
              text = "Save 10x more money than you pay",
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

      // Value proposition banner
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "If this app helps you avoid wasting KSh 100 on just one impulse purchase, it has already paid for itself!",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, lineHeight = 18.sp),
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Plan Selection Cards
      Text(
        text = "Choose Your Student Plan:",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))

      // 1. Weekly Plan (25 bob)
      PlanSelectionCard(
        title = "Student Weekly Pass",
        price = "KSh 25 / week",
        subtitle = "Lowest upfront friction — only 25 bob/week!",
        isSelected = selectedPlan == "WEEKLY_25",
        badge = "Easiest Start",
        onClick = { selectedPlan = "WEEKLY_25" }
      )

      Spacer(modifier = Modifier.height(8.dp))

      // 2. Monthly Plan (100 bob - recommended)
      PlanSelectionCard(
        title = "Monthly Saver",
        price = "KSh 100 / month",
        subtitle = "Only 1% of a KSh 10k budget — less than 1 fast-food snack",
        isSelected = selectedPlan == "MONTHLY_100",
        badge = "Most Popular (Recommended)",
        onClick = { selectedPlan = "MONTHLY_100" }
      )

      Spacer(modifier = Modifier.height(8.dp))

      // 3. Annual/Semester Plan (900)
      PlanSelectionCard(
        title = "Semester / Academic Year",
        price = "KSh 900 / year",
        subtitle = "Equivalent to KSh 75/mo (Save 25% for the full year)",
        isSelected = selectedPlan == "ANNUAL_900",
        badge = "Best Value",
        onClick = { selectedPlan = "ANNUAL_900" }
      )

      Spacer(modifier = Modifier.height(18.dp))

      // Included Student Features List
      Text(
        text = "What You Get with Pro:",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))

      val proPerks = listOf(
        "Unlimited 'Can I Afford This?' simulations (vs. 3/month on free)",
        "'Survive Until Month-End' pace alerts and runway deficit warnings",
        "'Save More Than You Pay' automated money leak detector & tips",
        "Upcoming recurring bill locks (Rent, Fibre, Hostels)",
        "Savings goals tracker with automated target dates",
        "Export full records to CSV & statement text"
      )

      proPerks.forEach { perk ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = IncomeGreen,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = perk,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Payment Method Section
      Text(
        text = "Pay via Mobile Money:",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // M-Pesa option
        Surface(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .border(
              width = if (paymentMethod == "M-PESA") 2.dp else 1.dp,
              color = if (paymentMethod == "M-PESA") MpesaGreen else MaterialTheme.colorScheme.outlineVariant,
              shape = RoundedCornerShape(12.dp)
            )
            .clickable { paymentMethod = "M-PESA" },
          color = if (paymentMethod == "M-PESA") MpesaGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = paymentMethod == "M-PESA",
              onClick = { paymentMethod = "M-PESA" }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "M-Pesa",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              color = MpesaGreen
            )
          }
        }

        // Airtel Money option
        Surface(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .border(
              width = if (paymentMethod == "AIRTEL") 2.dp else 1.dp,
              color = if (paymentMethod == "AIRTEL") AirtelRed else MaterialTheme.colorScheme.outlineVariant,
              shape = RoundedCornerShape(12.dp)
            )
            .clickable { paymentMethod = "AIRTEL" },
          color = if (paymentMethod == "AIRTEL") AirtelRed.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = paymentMethod == "AIRTEL",
              onClick = { paymentMethod = "AIRTEL" }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Airtel Money",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              color = AirtelRed
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedTextField(
        value = phoneNumber,
        onValueChange = { phoneNumber = it },
        label = { Text("Phone Number for STK Push") },
        leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("stk_phone_input"),
        shape = RoundedCornerShape(12.dp)
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Simulated STK Push Checkout Button
      if (isProcessingPayment) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
              Text(
                text = "Sending STK Push prompt to $phoneNumber...",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Please enter your PIN on your phone...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      } else if (paymentSuccessCode != null) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = IncomeGreen.copy(alpha = 0.15f))
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IncomeGreen)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Payment Confirmed! Code: $paymentSuccessCode",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = IncomeGreen
              )
              Text(
                text = "Welcome to MaliTrack Student Pro!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      } else {
        val planAmt = when (selectedPlan) {
          "WEEKLY_25" -> "KSh 25"
          "ANNUAL_900" -> "KSh 900"
          else -> "KSh 100"
        }

        Button(
          onClick = {
            isProcessingPayment = true
            scope.launch {
              delay(1600)
              val fakeCode = "QK" + (100000..999999).random() + "MP"
              paymentSuccessCode = fakeCode
              isProcessingPayment = false
              onUnlockPremium(selectedPlan)
              delay(800)
              onDismiss()
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("pay_stk_push_btn"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (paymentMethod == "M-PESA") MpesaGreen else AirtelRed
          )
        ) {
          Text(
            text = "Pay $planAmt with $paymentMethod STK Push",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        }
      }
    }
  }
}

@Composable
private fun PlanSelectionCard(
  title: String,
  price: String,
  subtitle: String,
  isSelected: Boolean,
  badge: String,
  onClick: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .border(
        width = if (isSelected) 2.dp else 1.dp,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        shape = RoundedCornerShape(14.dp)
      )
      .clickable { onClick() },
    shape = RoundedCornerShape(14.dp),
    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        RadioButton(
          selected = isSelected,
          onClick = onClick
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = title,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ) {
              Text(
                text = badge,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
          Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Text(
        text = price,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
      )
    }
  }
}
