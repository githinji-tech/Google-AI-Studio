package com.example.data.model

data class DailySpendMetrics(
  val totalAvailable: Double,
  val daysRemaining: Int,
  val upcomingCommitments: Double,
  val uncommittedRemaining: Double,
  val recommendedDailyAllowance: Double,
  val todaySpent: Double,
  val safeTodayRemaining: Double,
  val isOverToday: Boolean,
  val overAmount: Double,
  val adjustedTomorrowDaily: Double,
  val runwayDays: Int,
  val threeDayBurnRate: Double
)

data class AffordabilitySimulation(
  val itemName: String,
  val cost: Double,
  val currentDailyAllowance: Double,
  val newDailyAllowance: Double,
  val dailyDrop: Double,
  val remainingDays: Int,
  val verdict: AffordVerdict,
  val explanation: String
)

enum class AffordVerdict {
  SAFE,
  TIGHT_SQUEEZE,
  DANGEROUS
}

data class SpendingLeak(
  val category: String,
  val spentAmount: Double,
  val potentialSavings: Double,
  val studentTip: String,
  val monthsOfPremiumCovered: Double
)
