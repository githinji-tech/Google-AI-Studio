package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Emerald80,
    onPrimary = Color(0xFF003825),
    primaryContainer = Color(0xFF005138),
    onPrimaryContainer = Color(0xFF89F8C7),
    secondary = EmeraldGrey80,
    onSecondary = Color(0xFF1F3529),
    secondaryContainer = Color(0xFF354B3E),
    onSecondaryContainer = Color(0xFFCFE9D8),
    tertiary = Amber80,
    background = SurfaceDark,
    surface = SurfaceContainerDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Emerald40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF89F8C7),
    onPrimaryContainer = Color(0xFF002114),
    secondary = EmeraldGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFE9D8),
    onSecondaryContainer = Color(0xFF0A1F15),
    tertiary = Amber40,
    background = SurfaceLight,
    surface = SurfaceContainerLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight
  )

@Composable
fun FinanceTrackerTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep consistent finance brand colors by default
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
