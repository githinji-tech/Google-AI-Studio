package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_expenses")
data class RecurringExpenseEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val amount: Double,
  val category: String,
  val paymentMethod: String = "M-Pesa",
  val frequency: String = "MONTHLY", // "MONTHLY", "WEEKLY", "DAILY"
  val dueDay: Int = 1, // e.g. 1st of month, or 1=Monday for weekly
  val lastLoggedDate: Long? = null,
  val isActive: Boolean = true
)
