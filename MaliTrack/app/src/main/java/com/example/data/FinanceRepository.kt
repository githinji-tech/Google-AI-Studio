package com.example.data

import com.example.data.dao.FinanceDao
import com.example.data.model.BudgetEntity
import com.example.data.model.RecurringExpenseEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val dao: FinanceDao) {
  val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
  val allBudgets: Flow<List<BudgetEntity>> = dao.getAllBudgets()
  val allRecurringExpenses: Flow<List<RecurringExpenseEntity>> = dao.getAllRecurringExpenses()
  val allSavingsGoals: Flow<List<SavingsGoalEntity>> = dao.getAllSavingsGoals()

  fun getTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>> {
    return dao.getTransactionsBetween(startMillis, endMillis)
  }

  suspend fun getAllTransactionsList(): List<TransactionEntity> {
    return dao.getAllTransactionsList()
  }

  suspend fun getTransactionByReference(refCode: String): TransactionEntity? {
    return dao.getTransactionByReference(refCode)
  }

  suspend fun insertTransaction(transaction: TransactionEntity): Long {
    return dao.insertTransaction(transaction)
  }

  suspend fun insertTransactions(transactions: List<TransactionEntity>) {
    dao.insertTransactions(transactions)
  }

  suspend fun updateTransaction(transaction: TransactionEntity) {
    dao.updateTransaction(transaction)
  }

  suspend fun deleteTransaction(transaction: TransactionEntity) {
    dao.deleteTransaction(transaction)
  }

  suspend fun deleteTransactionById(id: Long) {
    dao.deleteTransactionById(id)
  }

  suspend fun clearAllTransactions() {
    dao.clearAllTransactions()
  }

  // Budgets
  suspend fun insertBudget(budget: BudgetEntity): Long {
    return dao.insertBudget(budget)
  }

  suspend fun updateBudget(budget: BudgetEntity) {
    dao.updateBudget(budget)
  }

  suspend fun deleteBudget(budget: BudgetEntity) {
    dao.deleteBudget(budget)
  }

  // Recurring
  suspend fun insertRecurringExpense(expense: RecurringExpenseEntity): Long {
    return dao.insertRecurringExpense(expense)
  }

  suspend fun updateRecurringExpense(expense: RecurringExpenseEntity) {
    dao.updateRecurringExpense(expense)
  }

  suspend fun deleteRecurringExpense(expense: RecurringExpenseEntity) {
    dao.deleteRecurringExpense(expense)
  }

  // Savings Goals
  suspend fun insertSavingsGoal(goal: SavingsGoalEntity): Long {
    return dao.insertSavingsGoal(goal)
  }

  suspend fun updateSavingsGoal(goal: SavingsGoalEntity) {
    dao.updateSavingsGoal(goal)
  }

  suspend fun deleteSavingsGoal(goal: SavingsGoalEntity) {
    dao.deleteSavingsGoal(goal)
  }
}
