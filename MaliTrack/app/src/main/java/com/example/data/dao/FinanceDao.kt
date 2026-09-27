package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BudgetEntity
import com.example.data.model.RecurringExpenseEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
  // Transactions
  @Query("SELECT * FROM transactions ORDER BY date DESC")
  fun getAllTransactions(): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions WHERE date >= :startMillis AND date <= :endMillis ORDER BY date DESC")
  fun getTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions ORDER BY date DESC")
  suspend fun getAllTransactionsList(): List<TransactionEntity>

  @Query("SELECT * FROM transactions WHERE referenceCode = :refCode LIMIT 1")
  suspend fun getTransactionByReference(refCode: String): TransactionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: TransactionEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransactions(transactions: List<TransactionEntity>)

  @Update
  suspend fun updateTransaction(transaction: TransactionEntity)

  @Delete
  suspend fun deleteTransaction(transaction: TransactionEntity)

  @Query("DELETE FROM transactions WHERE id = :id")
  suspend fun deleteTransactionById(id: Long)

  @Query("DELETE FROM transactions")
  suspend fun clearAllTransactions()

  // Budgets
  @Query("SELECT * FROM budgets")
  fun getAllBudgets(): Flow<List<BudgetEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBudget(budget: BudgetEntity): Long

  @Update
  suspend fun updateBudget(budget: BudgetEntity)

  @Delete
  suspend fun deleteBudget(budget: BudgetEntity)

  // Recurring Expenses
  @Query("SELECT * FROM recurring_expenses ORDER BY dueDay ASC")
  fun getAllRecurringExpenses(): Flow<List<RecurringExpenseEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRecurringExpense(expense: RecurringExpenseEntity): Long

  @Update
  suspend fun updateRecurringExpense(expense: RecurringExpenseEntity)

  @Delete
  suspend fun deleteRecurringExpense(expense: RecurringExpenseEntity)

  // Savings Goals
  @Query("SELECT * FROM savings_goals ORDER BY id DESC")
  fun getAllSavingsGoals(): Flow<List<SavingsGoalEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSavingsGoal(goal: SavingsGoalEntity): Long

  @Update
  suspend fun updateSavingsGoal(goal: SavingsGoalEntity)

  @Delete
  suspend fun deleteSavingsGoal(goal: SavingsGoalEntity)
}
