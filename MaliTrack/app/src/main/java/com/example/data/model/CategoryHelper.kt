package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryMeta(
  val name: String,
  val icon: ImageVector,
  val color: Color
)

object CategoryHelper {
  val EXPENSE_CATEGORIES = listOf(
    CategoryMeta("Food & Dining", Icons.Filled.Fastfood, Color(0xFFFF7043)),
    CategoryMeta("Groceries", Icons.Filled.ShoppingCart, Color(0xFF66BB6A)),
    CategoryMeta("Transport", Icons.Filled.DirectionsCar, Color(0xFF42A5F5)),
    CategoryMeta("Bills & Utilities", Icons.Filled.Lightbulb, Color(0xFFFFA726)),
    CategoryMeta("Housing", Icons.Filled.Home, Color(0xFF8D6E63)),
    CategoryMeta("Shopping", Icons.Filled.ShoppingBag, Color(0xFFAB47BC)),
    CategoryMeta("Entertainment", Icons.Filled.Movie, Color(0xFFEC407A)),
    CategoryMeta("Health", Icons.Filled.LocalHospital, Color(0xFF26A69A)),
    CategoryMeta("Education", Icons.Filled.School, Color(0xFF5C6BC0)),
    CategoryMeta("Uncategorized", Icons.Filled.HelpOutline, Color(0xFFFFB300)),
    CategoryMeta("Other", Icons.Filled.Category, Color(0xFF78909C))
  )

  val INCOME_CATEGORIES = listOf(
    CategoryMeta("Salary", Icons.Filled.Payments, Color(0xFF2E7D32)),
    CategoryMeta("Business", Icons.Filled.Business, Color(0xFF00897B)),
    CategoryMeta("Investment", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF1565C0)),
    CategoryMeta("Direct Transfer", Icons.Filled.AccountBalance, Color(0xFF00ACC1)),
    CategoryMeta("Other", Icons.Filled.Category, Color(0xFF78909C))
  )

  fun getCategoryMeta(name: String): CategoryMeta {
    if (name.equals("Uncategorized", ignoreCase = true)) {
      return CategoryMeta("Uncategorized", Icons.Filled.HelpOutline, Color(0xFFFFB300))
    }
    return EXPENSE_CATEGORIES.firstOrNull { it.name.equals(name, ignoreCase = true) }
      ?: INCOME_CATEGORIES.firstOrNull { it.name.equals(name, ignoreCase = true) }
      ?: CategoryMeta(name, Icons.Filled.Category, Color(0xFF78909C))
  }
}
