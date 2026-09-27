package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryHelper
import com.example.parser.MobileMoneyParser
import com.example.parser.ParsedTransaction
import com.example.ui.theme.AirtelRed
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.WarningAmber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ParserScreen(
  onBack: () -> Unit,
  onImportTransactions: (List<ParsedTransaction>) -> Unit,
  currencySymbol: String = "KSh"
) {
  val context = LocalContext.current
  var inputText by remember { mutableStateOf("") }
  var parsedList by remember { mutableStateOf<List<ParsedTransaction>>(emptyList()) }
  var hasParsed by remember { mutableStateOf(false) }
  var transactionToPromptCategory by remember { mutableStateOf<ParsedTransaction?>(null) }

  // File Picker for statements (.txt, .csv, text files)
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let {
      try {
        context.contentResolver.openInputStream(it)?.use { stream ->
          val reader = BufferedReader(InputStreamReader(stream))
          val content = reader.readText()
          inputText = content
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  // Sample MPESA text
  val sampleMpesa = "QA12BC34DE Confirmed. Ksh1,850.00 sent to Naivas Supermarket 0712345678 on 24/9/26 at 2:30 PM. New M-PESA balance is Ksh4,200.00.\n\nQB34CD56EF Confirmed. You have received Ksh7,500.00 from Jane Kamau 0722000000 on 24/9/26 at 10:15 AM. New M-PESA balance is Ksh11,700.00.\n\nQC56EF78GH Confirmed. Ksh2,100.00 sent to Kenya Power for account 12345678 on 23/9/26 at 6:45 PM. New M-PESA balance is Ksh9,600.00."

  // Sample Airtel Money text
  val sampleAirtel = "Txn ID: MP260924.1234.A12345. You have paid Ksh 650.00 to QuickMart Supermarket on 24/09/2026 14:20. Balance: Ksh 3,100.00\n\nTxn ID: MP260924.5678.B67890. You have received Ksh 3,000.00 from David Ochieng on 23/09/2026 09:30."

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Parse Mobile Money",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("parser_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    },
    bottomBar = {
      if (parsedList.isNotEmpty()) {
        val selectedCount = parsedList.count { it.isSelected }
        Surface(
          tonalElevation = 8.dp,
          shadowElevation = 8.dp,
          color = MaterialTheme.colorScheme.surface
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "$selectedCount selected",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Ready to add to records",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Button(
              onClick = {
                val uncategorized = parsedList.filter { it.isSelected && (it.category == "Uncategorized" || it.needsCategoryPrompt) }
                if (uncategorized.isNotEmpty()) {
                  transactionToPromptCategory = uncategorized.first()
                } else {
                  onImportTransactions(parsedList)
                  onBack()
                }
              },
              enabled = selectedCount > 0,
              modifier = Modifier.testTag("confirm_import_button"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
              Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Import Records", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  ) { innerPadding ->
    // Category Prompt Modal for Uncategorized Transactions
    if (transactionToPromptCategory != null) {
      val txToCategorize = transactionToPromptCategory!!
      AlertDialog(
        onDismissRequest = { transactionToPromptCategory = null },
        icon = {
          Icon(
            imageVector = Icons.Default.HelpOutline,
            contentDescription = null,
            tint = WarningAmber,
            modifier = Modifier.size(32.dp)
          )
        },
        title = {
          Text(
            text = "Select Category",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
          )
        },
        text = {
          Column {
            Text(
              text = "${txToCategorize.title} (${if (txToCategorize.type == "INCOME") "+" else "-"}$currencySymbol ${String.format(Locale.US, "%,.2f", txToCategorize.amount)})",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Nothing in this transaction identified what it was for. What was this purchase for?",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              val availableCategories = if (txToCategorize.type == "INCOME") {
                CategoryHelper.INCOME_CATEGORIES
              } else {
                CategoryHelper.EXPENSE_CATEGORIES.filter { it.name != "Uncategorized" }
              }

              availableCategories.forEach { cat ->
                Surface(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                      val currentId = txToCategorize.id
                      parsedList = parsedList.map {
                        if (it.id == currentId) it.copy(category = cat.name, needsCategoryPrompt = false) else it
                      }
                      val remaining = parsedList.filter {
                        it.isSelected && it.id != currentId && (it.category == "Uncategorized" || it.needsCategoryPrompt)
                      }
                      if (remaining.isNotEmpty()) {
                        transactionToPromptCategory = remaining.first()
                      } else {
                        transactionToPromptCategory = null
                        onImportTransactions(parsedList)
                        onBack()
                      }
                    },
                  color = cat.color.copy(alpha = 0.15f),
                  shape = RoundedCornerShape(8.dp),
                  border = androidx.compose.foundation.BorderStroke(1.dp, cat.color.copy(alpha = 0.5f))
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = cat.icon,
                      contentDescription = null,
                      tint = cat.color,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = cat.name,
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = cat.color
                    )
                  }
                }
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { transactionToPromptCategory = null }) {
            Text("Cancel")
          }
        }
      )
    }
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "Paste your M-Pesa or Airtel Money SMS confirmation messages, or upload a statement. finTracker extracts dates, amounts, merchants, and categories automatically offline.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      item {
        // Quick preset chips
        Text(
          text = "Try Sample Message:",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = {
              inputText = sampleMpesa
              parsedList = MobileMoneyParser.parseMessages(sampleMpesa)
              hasParsed = true
            },
            shape = RoundedCornerShape(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MpesaGreen)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("M-Pesa SMS", fontSize = 12.sp)
          }

          OutlinedButton(
            onClick = {
              inputText = sampleAirtel
              parsedList = MobileMoneyParser.parseMessages(sampleAirtel)
              hasParsed = true
            },
            shape = RoundedCornerShape(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(AirtelRed)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Airtel Money SMS", fontSize = 12.sp)
          }
        }
      }

      item {
        // Paste area
        OutlinedTextField(
          value = inputText,
          onValueChange = { inputText = it },
          label = { Text("Paste SMS or Statement Text") },
          placeholder = { Text("e.g. QA12BC34DE Confirmed. Ksh1,500.00 sent to...") },
          modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .testTag("sms_input_field"),
          shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Action Buttons Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Paste from Clipboard
          OutlinedButton(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val clip = clipboard.primaryClip
              if (clip != null && clip.itemCount > 0) {
                inputText = clip.getItemAt(0).text?.toString() ?: ""
              }
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Paste", fontSize = 12.sp)
          }

          // Upload statement file
          OutlinedButton(
            onClick = { filePickerLauncher.launch("text/*") },
            modifier = Modifier.weight(1.2f),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Upload File", fontSize = 12.sp)
          }

          if (inputText.isNotBlank()) {
            IconButton(onClick = {
              inputText = ""
              parsedList = emptyList()
              hasParsed = false
            }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
          }
        }
      }

      item {
        Button(
          onClick = {
            parsedList = MobileMoneyParser.parseMessages(inputText)
            hasParsed = true
          },
          enabled = inputText.isNotBlank(),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("parse_button"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Parse Messages", fontWeight = FontWeight.Bold)
        }
      }

      if (hasParsed) {
        item {
          if (parsedList.isEmpty()) {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = "No valid transactions detected.",
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Make sure the text includes standard M-Pesa or Airtel Money confirmation formats.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          } else {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Parsed Results (${parsedList.size})",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                  text = "Tap to toggle selection",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              val uncategorizedCount = parsedList.count { it.isSelected && (it.category == "Uncategorized" || it.needsCategoryPrompt) }
              if (uncategorizedCount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(12.dp),
                  color = WarningAmber.copy(alpha = 0.15f),
                  border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f))
                ) {
                  Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.HelpOutline,
                      contentDescription = null,
                      tint = WarningAmber,
                      modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                      text = "$uncategorizedCount transaction(s) have no category keywords (e.g. Google Play, transfers). Please select what they were for before importing.",
                      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                }
              }
            }
          }
        }

        items(parsedList, key = { it.id }) { item ->
          ParsedTransactionCard(
            item = item,
            currencySymbol = currencySymbol,
            onToggleSelect = {
              parsedList = parsedList.map {
                if (it.id == item.id) it.copy(isSelected = !it.isSelected) else it
              }
            },
            onUpdateTitle = { newTitle ->
              parsedList = parsedList.map {
                if (it.id == item.id) it.copy(title = newTitle) else it
              }
            },
            onUpdateCategory = { newCategory ->
              parsedList = parsedList.map {
                if (it.id == item.id) it.copy(category = newCategory) else it
              }
            }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(40.dp))
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParsedTransactionCard(
  item: ParsedTransaction,
  currencySymbol: String,
  onToggleSelect: () -> Unit,
  onUpdateTitle: (String) -> Unit,
  onUpdateCategory: (String) -> Unit
) {
  val isIncome = item.type == "INCOME"
  val dateFormat = SimpleDateFormat("dd MMM, h:mm a", Locale.US)
  val formattedDate = dateFormat.format(Date(item.date))
  val brandColor = if (item.paymentMethod.contains("Airtel", ignoreCase = true)) AirtelRed else MpesaGreen

  var showCategoryPicker by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onToggleSelect() },
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (item.isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ),
    border = if (item.isSelected) {
      androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else null,
    elevation = CardDefaults.cardElevation(defaultElevation = if (item.isSelected) 2.dp else 0.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(
            checked = item.isSelected,
            onCheckedChange = { onToggleSelect() }
          )

          // Provider badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(brandColor.copy(alpha = 0.15f))
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = item.paymentMethod,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = brandColor
            )
          }

          if (item.referenceCode.isNotBlank()) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = item.referenceCode,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Amount
        Text(
          text = "${if (isIncome) "+" else "-"}$currencySymbol ${String.format(Locale.US, "%,.2f", item.amount)}",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = if (isIncome) IncomeGreen else ExpenseRed
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Title & Date
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = item.title,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface
        )

        Text(
          text = formattedDate,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      val isUncategorized = item.category == "Uncategorized" || item.needsCategoryPrompt

      if (isUncategorized) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp),
          shape = RoundedCornerShape(10.dp),
          color = WarningAmber.copy(alpha = 0.12f),
          border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.4f))
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.HelpOutline,
                contentDescription = null,
                tint = WarningAmber,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "No keyword found — What was this for?",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              val categories = if (isIncome) {
                CategoryHelper.INCOME_CATEGORIES
              } else {
                CategoryHelper.EXPENSE_CATEGORIES.filter { it.name != "Uncategorized" }
              }
              categories.forEach { cat ->
                Surface(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onUpdateCategory(cat.name) },
                  color = cat.color.copy(alpha = 0.15f),
                  shape = RoundedCornerShape(6.dp),
                  border = androidx.compose.foundation.BorderStroke(1.dp, cat.color.copy(alpha = 0.35f))
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(imageVector = cat.icon, contentDescription = null, tint = cat.color, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = cat.name,
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                      color = cat.color
                    )
                  }
                }
              }
            }
          }
        }
      } else {
        // Category chip (clickable to change)
        Row(
          modifier = Modifier
            .padding(start = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { showCategoryPicker = !showCategoryPicker }
            .padding(horizontal = 10.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          val meta = CategoryHelper.getCategoryMeta(item.category)
          Icon(
            imageVector = meta.icon,
            contentDescription = null,
            tint = meta.color,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${item.category} (tap to change)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        if (showCategoryPicker) {
          Spacer(modifier = Modifier.height(10.dp))
          FlowRow(
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            val categories = if (isIncome) CategoryHelper.INCOME_CATEGORIES else CategoryHelper.EXPENSE_CATEGORIES.filter { it.name != "Uncategorized" }
            categories.forEach { cat ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (cat.name == item.category) cat.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                  .clickable {
                    onUpdateCategory(cat.name)
                    showCategoryPicker = false
                  }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = cat.name,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (cat.name == item.category) FontWeight.Bold else FontWeight.Normal
                  ),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }
    }
  }
}
