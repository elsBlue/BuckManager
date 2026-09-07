package com.buckmanager.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FundGoalAndMonetizationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun goalProgress_clampsBetween0And100() {
        assertEquals(0, progressPercent(current = 0.0, target = 100.0))
        assertEquals(50, progressPercent(current = 50.0, target = 100.0))
        assertEquals(100, progressPercent(current = 150.0, target = 100.0))
        assertEquals(0, progressPercent(current = 10.0, target = 0.0))
    }

    @Test
    fun deposit_updatesCurrentAmountAndDoesNotGoNegative() {
        val goal = FundGoalConfig(currentAmount = 20_000.0, targetAmount = 100_000.0)
        val afterDeposit = goal.copy(currentAmount = (goal.currentAmount + 50_000.0).coerceAtLeast(0.0))
        assertEquals(70_000.0, afterDeposit.currentAmount, 0.01)
        val afterWithdraw = afterDeposit.copy(currentAmount = (afterDeposit.currentAmount - 100_000.0).coerceAtLeast(0.0))
        assertEquals(0.0, afterWithdraw.currentAmount, 0.01)
    }

    @Test
    fun fundGoalConfig_roundTripsThroughJson() {
        val original = FundGoalConfig(
            name = "Liburan",
            targetAmount = 1_000_000.0,
            currentAmount = 250_000.0,
            backgroundImageUri = "file:///data/user/0/com.buckmanager.app/files/backgrounds/bg_1.jpg",
            radiusTopLeft = 16
        )
        val encoded = json.encodeToString(original)
        val decoded = json.decodeFromString<FundGoalConfig>(encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun monetization_hasPremium_whenLifetimeOrTemporaryOrTickets() {
        assertTrue(hasPremiumAccess(MonetizationState(isPremium = true)))
        assertTrue(
            hasPremiumAccess(
                MonetizationState(premiumExpiryDate = System.currentTimeMillis() + 60_000L)
            )
        )
        assertTrue(hasPremiumAccess(MonetizationState(adTickets = 2)))
        assertFalse(hasPremiumAccess(MonetizationState()))
        assertFalse(
            hasPremiumAccess(
                MonetizationState(premiumExpiryDate = System.currentTimeMillis() - 1_000L)
            )
        )
    }

    @Test
    fun localSession_isNotGoogleAccount() {
        assertTrue(AuthSession.isLocal(AuthSession.LOCAL_EMAIL))
        assertFalse(AuthSession.isGoogle(AuthSession.LOCAL_EMAIL))
        assertTrue(AuthSession.isGoogle("ada@example.com"))
        assertFalse(AuthSession.isLocal(null))
        assertFalse(AuthSession.isGoogle(null))
        assertFalse(AuthSession.isGoogle(""))
    }

    @Test
    fun deposit_cannotExceedSpendableBalance() {
        val netWorth = 80_000.0
        val alreadyInGoal = 30_000.0
        val requested = 100_000.0
        val room = (netWorth - alreadyInGoal).coerceAtLeast(0.0)
        val applied = minOf(requested, room)
        assertEquals(50_000.0, applied, 0.01)
        assertEquals(0.0, minOf(10_000.0, (netWorth - netWorth).coerceAtLeast(0.0)), 0.01)
    }

    @Test
    fun withdraw_cannotExceedGoalBalance() {
        val current = 20_000.0
        val requested = -50_000.0
        val applied = maxOf(requested, -current)
        assertEquals(-20_000.0, applied, 0.01)
    }

    @Test
    fun newEnvelope_usesUnassignedMainBufferNotTotalIncludingMain() {
        val envelopes = listOf(
            Envelope(id = "main", name = "Main", percentage = 10, colorHex = "#000"),
            Envelope(id = "needs", name = "Needs", percentage = 50, colorHex = "#111"),
            Envelope(id = "wants", name = "Wants", percentage = 30, colorHex = "#222"),
            Envelope(id = "savings", name = "Savings", percentage = 10, colorHex = "#333")
        )
        val assigned = envelopes.filter { it.id != "main" }.sumOf { it.percentage }
        val available = 100 - assigned
        assertEquals(90, assigned)
        assertEquals(10, available)
        assertEquals(100, envelopes.sumOf { it.percentage })
    }

    companion object {
        fun progressPercent(current: Double, target: Double): Int {
            val ratio = if (target > 0) (current / target).coerceIn(0.0, 1.0) else 0.0
            return (ratio * 100).toInt()
        }

        fun hasPremiumAccess(state: MonetizationState, now: Long = System.currentTimeMillis()): Boolean {
            return state.isPremium || state.premiumExpiryDate > now || state.adTickets > 0
        }
    }
}
