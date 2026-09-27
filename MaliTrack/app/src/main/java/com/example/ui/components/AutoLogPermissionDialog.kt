package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.WarningAmber

@Composable
fun AutoLogPermissionDialog(
  onDismiss: () -> Unit,
  onPermissionsGranted: () -> Unit
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

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) {
    hasSmsPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    hasNotificationListener = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
    onPermissionsGranted()
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
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
        Text(
          text = "Auto-Log Transactions",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Allow MaliTrack to read SMS and Notifications so it can automatically detect and log M-Pesa, Airtel Money, and bank transactions without any manual input.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Feature 1: SMS Reading
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.Top
        ) {
          Icon(
            imageVector = if (hasSmsPermission) Icons.Default.CheckCircle else Icons.Default.MarkEmailRead,
            contentDescription = null,
            tint = if (hasSmsPermission) IncomeGreen else MaterialTheme.colorScheme.primary,
            modifier = Modifier
              .size(22.dp)
              .padding(top = 2.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "1. Messages (SMS) Access",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = if (hasSmsPermission) "Permission Granted ✓ Ready to auto-log SMS" else "Reads incoming mobile money and bank messages to log income & expenses in real-time.",
              style = MaterialTheme.typography.bodySmall,
              color = if (hasSmsPermission) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Feature 2: Notification Listener
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.Top
        ) {
          Icon(
            imageVector = if (hasNotificationListener) Icons.Default.CheckCircle else Icons.Default.NotificationsActive,
            contentDescription = null,
            tint = if (hasNotificationListener) IncomeGreen else MaterialTheme.colorScheme.primary,
            modifier = Modifier
              .size(22.dp)
              .padding(top = 2.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "2. Notification Access",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = if (hasNotificationListener) "Access Enabled ✓ Monitoring notifications" else "Catches push notifications from finance and banking apps (e.g. Safaricom App, bank alerts).",
              style = MaterialTheme.typography.bodySmall,
              color = if (hasNotificationListener) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Private & on-device: MaliTrack only parses financial statements and never shares your SMS data.",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (!hasSmsPermission) {
            val perms = mutableListOf(
              Manifest.permission.RECEIVE_SMS,
              Manifest.permission.READ_SMS
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              perms.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionLauncher.launch(perms.toTypedArray())
          } else if (!hasNotificationListener) {
            try {
              context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            } catch (e: Exception) {
              e.printStackTrace()
            }
          } else {
            onDismiss()
          }
        },
        modifier = Modifier.testTag("dialog_grant_permissions_btn"),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Text(
          text = when {
            !hasSmsPermission -> "Grant SMS Permission"
            !hasNotificationListener -> "Enable Notification Access"
            else -> "Done"
          }
        )
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("dialog_dismiss_permissions_btn")
      ) {
        Text("Later")
      }
    }
  )
}
