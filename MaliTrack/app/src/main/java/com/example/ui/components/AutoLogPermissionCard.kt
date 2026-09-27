package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.WarningAmber

@Composable
fun AutoLogPermissionCard(
  onSyncInbox: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  var hasSmsPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    )
  }

  var hasNotificationListener by remember {
    mutableStateOf(
      NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
    )
  }

  // Refresh status when returning to app
  fun refreshPermissions() {
    hasSmsPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    hasNotificationListener = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
  }

  // Multi-permission launcher for SMS & Post Notifications
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { perms ->
    refreshPermissions()
    if (hasSmsPermission) {
      onSyncInbox()
    }
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("auto_log_permission_card"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    )
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
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.MarkEmailRead,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Auto-Log Mobile Money",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Read SMS & Notifications automatically",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(18.dp))
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "MaliTrack can automatically read your M-Pesa, Airtel Money, and bank SMS and notifications to log every transaction without typing.",
        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Status indicators
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // SMS Status Chip
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (hasSmsPermission) IncomeGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
          modifier = Modifier.weight(1f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (hasSmsPermission) Icons.Default.CheckCircle else Icons.Default.MarkEmailRead,
              contentDescription = null,
              tint = if (hasSmsPermission) IncomeGreen else WarningAmber,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (hasSmsPermission) "SMS: Active" else "SMS: Grant Perm",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = if (hasSmsPermission) IncomeGreen else WarningAmber
            )
          }
        }

        // Notification Listener Status Chip
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (hasNotificationListener) IncomeGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
          modifier = Modifier.weight(1f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (hasNotificationListener) Icons.Default.CheckCircle else Icons.Default.NotificationsActive,
              contentDescription = null,
              tint = if (hasNotificationListener) IncomeGreen else WarningAmber,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (hasNotificationListener) "Notif: Active" else "Notif: Access",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = if (hasNotificationListener) IncomeGreen else WarningAmber
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (!hasSmsPermission) {
          Button(
            onClick = {
              val perms = mutableListOf(
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
              )
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms.add(Manifest.permission.POST_NOTIFICATIONS)
              }
              permissionLauncher.launch(perms.toTypedArray())
            },
            modifier = Modifier
              .weight(1f)
              .testTag("grant_sms_permission_btn"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
          ) {
            Text(
              text = "Grant SMS Access",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
        }

        if (!hasNotificationListener) {
          OutlinedButton(
            onClick = {
              try {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                context.startActivity(intent)
              } catch (e: Exception) {
                e.printStackTrace()
              }
            },
            modifier = Modifier
              .weight(1f)
              .testTag("enable_notif_listener_btn"),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(
              text = "Notification Access",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
        }

        if (hasSmsPermission) {
          Button(
            onClick = onSyncInbox,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("sync_sms_inbox_btn"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Sync & Auto-Log from SMS Inbox",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
              )
            }
          }
        }
      }
    }
  }
}
