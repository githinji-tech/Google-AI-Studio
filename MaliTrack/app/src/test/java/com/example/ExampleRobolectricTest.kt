package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.parser.MobileMoneyParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MaliTrack", appName)
  }

  @Test
  fun `parse mpesa message correctly`() {
    val sms = "QA12BC34DE Confirmed. Ksh1,500.00 sent to Naivas Supermarket 0712345678 on 24/9/26 at 2:30 PM. New M-PESA balance is Ksh4,200.00."
    val parsed = MobileMoneyParser.parseSingleMessage(sms)

    org.junit.Assert.assertNotNull(parsed)
    assertEquals("QA12BC34DE", parsed?.referenceCode)
    assertEquals("EXPENSE", parsed?.type)
    assertEquals(1500.0, parsed?.amount ?: 0.0, 0.01)
    assertTrue(parsed?.title?.contains("Naivas", ignoreCase = true) == true)
    assertEquals("Food & Dining", parsed?.category)
  }

  @Test
  fun `parse airtel money message correctly`() {
    val sms = "Txn ID: MP260924.5678.B67890. You have received Ksh 3,000.00 from David Ochieng on 23/09/2026 09:30."
    val parsed = MobileMoneyParser.parseSingleMessage(sms)

    org.junit.Assert.assertNotNull(parsed)
    assertEquals("MP260924.5678.B67890", parsed?.referenceCode)
    assertEquals("INCOME", parsed?.type)
    assertEquals(3000.0, parsed?.amount ?: 0.0, 0.01)
    assertEquals("Airtel Money", parsed?.paymentMethod)
  }

  @Test
  fun `test preferences manager persists premium state`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = com.example.data.PreferencesManager(context)

    prefs.isPremium = false
    assertEquals(false, prefs.isPremium)

    prefs.isPremium = true
    assertEquals(true, prefs.isPremium)

    prefs.premiumPlan = "MONTHLY_100"
    assertEquals("MONTHLY_100", prefs.premiumPlan)
    prefs.premiumPlan = "WEEKLY_25"
    assertEquals("WEEKLY_25", prefs.premiumPlan)
  }

  @Test
  fun `test student affordability simulation calculation and debt avoidance`() {
    val metrics = com.example.data.model.DailySpendMetrics(
      totalAvailable = 7500.0,
      daysRemaining = 23,
      upcomingCommitments = 0.0,
      uncommittedRemaining = 7500.0,
      recommendedDailyAllowance = 326.0,
      todaySpent = 0.0,
      safeTodayRemaining = 326.0,
      isOverToday = false,
      overAmount = 0.0,
      adjustedTomorrowDaily = 326.0,
      runwayDays = 23,
      threeDayBurnRate = 300.0
    )

    val remainingAfter = metrics.uncommittedRemaining - 800.0
    val newDaily = remainingAfter / metrics.daysRemaining
    val drop = metrics.recommendedDailyAllowance - newDaily

    assertTrue("New daily allowance should be approximately 291", newDaily > 290.0 && newDaily < 292.0)
    assertTrue("Drop should be around 35", drop > 34.0 && drop < 36.0)
  }

  @Test
  fun `test freemium afford checks limit is 3 per month and resets`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = com.example.data.PreferencesManager(context)

    prefs.affordChecksCount = 0
    assertEquals(0, prefs.affordChecksCount)

    val count1 = prefs.incrementAffordChecks()
    assertEquals(1, count1)
    assertEquals(2, (com.example.data.PreferencesManager.MAX_FREE_AFFORD_CHECKS_PER_MONTH - count1).coerceAtLeast(0))

    val count2 = prefs.incrementAffordChecks()
    assertEquals(2, count2)
    assertEquals(1, (com.example.data.PreferencesManager.MAX_FREE_AFFORD_CHECKS_PER_MONTH - count2).coerceAtLeast(0))

    val count3 = prefs.incrementAffordChecks()
    assertEquals(3, count3)
    assertEquals(0, (com.example.data.PreferencesManager.MAX_FREE_AFFORD_CHECKS_PER_MONTH - count3).coerceAtLeast(0))
  }

  @Test
  fun `parse google play store message requires category confirmation and is not health`() {
    val sms = "QK79XX88YY Confirmed. Ksh250.00 paid to GOOGLE PLAY STORE. on 24/9/26 at 2:30 PM. New M-PESA balance is Ksh1,200.00. Transaction cost, Ksh0.00."
    val parsed = MobileMoneyParser.parseSingleMessage(sms)

    org.junit.Assert.assertNotNull(parsed)
    assertEquals("QK79XX88YY", parsed?.referenceCode)
    assertEquals("EXPENSE", parsed?.type)
    assertEquals(250.0, parsed?.amount ?: 0.0, 0.01)
    assertTrue("Title should contain Google Play", parsed?.title?.contains("GOOGLE PLAY", ignoreCase = true) == true)
    assertEquals("Uncategorized", parsed?.category)
    assertTrue("Should flag that it needs category prompt", parsed?.needsCategoryPrompt == true)
  }
}
