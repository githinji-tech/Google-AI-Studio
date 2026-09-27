package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val category: String, // "ALL" for total monthly budget, or specific category
  val monthKey: String = "DEFAULT", // "YYYY-MM" or "DEFAULT"
  val limitAmount: Double
)
