package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserAccount
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.MpesaGreen

@Composable
fun AuthScreen(
  onLoginSuccess: (UserAccount) -> Unit,
  onPerformLogin: (emailOrPhone: String, pass: String) -> Result<UserAccount>,
  onPerformRegister: (fullName: String, emailOrPhone: String, pass: String) -> Result<UserAccount>,
  modifier: Modifier = Modifier
) {
  // 0 = Log In, 1 = Create Account
  var selectedTab by remember { mutableIntStateOf(0) }

  // Form Fields
  var fullName by remember { mutableStateOf("") }
  var emailOrPhone by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }

  var passwordVisible by remember { mutableStateOf(false) }
  var confirmPasswordVisible by remember { mutableStateOf(false) }

  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isSubmitting by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()

  fun submit() {
    errorMessage = null
    if (selectedTab == 0) {
      // Log In
      if (emailOrPhone.isBlank()) {
        errorMessage = "Please enter your email or phone number"
        return
      }
      if (password.isBlank()) {
        errorMessage = "Please enter your password"
        return
      }

      isSubmitting = true
      val res = onPerformLogin(emailOrPhone.trim(), password)
      isSubmitting = false
      res.onSuccess { user ->
        onLoginSuccess(user)
      }.onFailure { err ->
        errorMessage = err.message ?: "Login failed. Please check your credentials."
      }
    } else {
      // Create Account
      if (fullName.isBlank()) {
        errorMessage = "Please enter your full name"
        return
      }
      if (emailOrPhone.isBlank()) {
        errorMessage = "Please enter your email or phone number"
        return
      }
      if (password.length < 4) {
        errorMessage = "Password must be at least 4 characters long"
        return
      }
      if (password != confirmPassword) {
        errorMessage = "Passwords do not match. Please verify."
        return
      }

      isSubmitting = true
      val res = onPerformRegister(fullName.trim(), emailOrPhone.trim(), password)
      isSubmitting = false
      res.onSuccess { user ->
        onLoginSuccess(user)
      }.onFailure { err ->
        errorMessage = err.message ?: "Account creation failed. Please try again."
      }
    }
  }

  Surface(
    modifier = modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .imePadding()
        .padding(horizontal = 24.dp, vertical = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // App Brand Logo
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(
            Brush.linearGradient(
              colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.tertiary
              )
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.AccountBalanceWallet,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(36.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "MaliTrack",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = (-0.5).sp
        ),
        color = MaterialTheme.colorScheme.onBackground
      )

      Text(
        text = "Student Smart Finance & Auto-Log Tracking",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Tab selector: Log In vs Create Account
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      ) {
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color.Transparent,
          divider = {},
          modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = {
              selectedTab = 0
              errorMessage = null
            },
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .testTag("auth_tab_login"),
            text = {
              Text(
                text = "Log In",
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
              )
            }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = {
              selectedTab = 1
              errorMessage = null
            },
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .testTag("auth_tab_register"),
            text = {
              Text(
                text = "Create Account",
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
              )
            }
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Error message banner
      AnimatedVisibility(
        visible = errorMessage != null,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.12f))
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.ErrorOutline,
              contentDescription = null,
              tint = ExpenseRed,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = errorMessage.orEmpty(),
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
              color = ExpenseRed
            )
          }
        }
      }

      // Input Form
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Name field (Only in Create Account)
        if (selectedTab == 1) {
          OutlinedTextField(
            value = fullName,
            onValueChange = {
              fullName = it
              errorMessage = null
            },
            label = { Text("Full Name") },
            placeholder = { Text("e.g. Collins Githinji") },
            leadingIcon = {
              Icon(Icons.Default.Person, contentDescription = null)
            },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("auth_input_name"),
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Text,
              imeAction = ImeAction.Next
            )
          )
        }

        // Email or Phone field
        OutlinedTextField(
          value = emailOrPhone,
          onValueChange = {
            emailOrPhone = it
            errorMessage = null
          },
          label = { Text("Email or Phone Number") },
          placeholder = { Text("e.g. 0712345678 or you@email.com") },
          leadingIcon = {
            Icon(Icons.Default.Email, contentDescription = null)
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("auth_input_identifier"),
          shape = RoundedCornerShape(14.dp),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
          )
        )

        // Password field
        OutlinedTextField(
          value = password,
          onValueChange = {
            password = it
            errorMessage = null
          },
          label = { Text("Password") },
          placeholder = { Text("Enter your password") },
          leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = null)
          },
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(
                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = if (passwordVisible) "Hide password" else "Show password"
              )
            }
          },
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("auth_input_password"),
          shape = RoundedCornerShape(14.dp),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = if (selectedTab == 1) ImeAction.Next else ImeAction.Done
          ),
          keyboardActions = KeyboardActions(
            onDone = { submit() }
          )
        )

        // Confirm Password field (Only in Create Account)
        if (selectedTab == 1) {
          OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
              confirmPassword = it
              errorMessage = null
            },
            label = { Text("Confirm Password") },
            placeholder = { Text("Re-enter your password") },
            leadingIcon = {
              Icon(Icons.Default.Lock, contentDescription = null)
            },
            trailingIcon = {
              IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                Icon(
                  imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                )
              }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("auth_input_confirm_password"),
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Password,
              imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
              onDone = { submit() }
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Submit Button
      Button(
        onClick = { submit() },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("auth_submit_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary
        ),
        enabled = !isSubmitting
      ) {
        if (isSubmitting) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp
          )
        } else {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = if (selectedTab == 0) "Log In to MaliTrack" else "Create My Account",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Toggle link at the bottom
      TextButton(
        onClick = {
          selectedTab = if (selectedTab == 0) 1 else 0
          errorMessage = null
        }
      ) {
        Text(
          text = if (selectedTab == 0) "Don't have an account? Create one now" else "Already have an account? Log In",
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.primary
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Informative security note
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
      ) {
        Text(
          text = "🔒 Your financial records and credentials remain encrypted on your device. Auto-logged transactions from SMS & Notifications are securely stored locally.",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(12.dp)
        )
      }
    }
  }
}
