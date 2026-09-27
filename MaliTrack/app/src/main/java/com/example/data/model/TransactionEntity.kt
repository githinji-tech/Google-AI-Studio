package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val type: String, // "EXPENSE" or "INCOME"
  val amount: Double,
  val title: String,
  val category: String,
  val date: Long = System.currentTimeMillis(),
  val paymentMethod: String = "M-Pesa",
  val referenceCode: String = "",
  val notes: String = ""
) {
  val isExpense: Boolean get() = type == "EXPENSE"
  val isIncome: Boolean get() = type == "INCOME"
}
