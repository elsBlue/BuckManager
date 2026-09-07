package com.buckmanager.app.model

import com.buckmanager.app.utils.ThemePackUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ThemePackTest {

    @Test
    fun zipSlip_isRejected() {
        val dest = File("/tmp/buck-theme-test").apply { mkdirs() }
        assertNull(ThemePackUtils.resolveZipEntry(dest, "../evil.jpg"))
        assertNull(ThemePackUtils.resolveZipEntry(dest, "/etc/passwd"))
        assertNull(ThemePackUtils.resolveZipEntry(dest, "..\\windows"))
        assertNotNull(ThemePackUtils.resolveZipEntry(dest, "images/wallpaper.jpg"))
        assertNotNull(ThemePackUtils.resolveZipEntry(dest, "theme.json"))
    }

    @Test
    fun fileStem_isSafe() {
        assertEquals("Sunset_Gold", ThemePackUtils.sanitizeFileStem("Sunset Gold"))
        assertEquals("BuckLook", ThemePackUtils.sanitizeFileStem("***"))
        assertTrue(ThemePackUtils.sanitizeFileStem("a".repeat(80)).length <= 40)
    }

    @Test
    fun mergeEnvelopeLook_keepsMoneyFields() {
        val current = Envelope(id = "needs", name = "Needs", percentage = 50, colorHex = "#111111")
        val packed = Envelope(
            id = "needs",
            name = "Someone else",
            percentage = 10,
            colorHex = "#FF0000",
            backgroundColorHex = "#FFFFFF",
            labelColorHex = "#00FF00"
        )
        val merged = ThemePackUtils.mergeEnvelopeLook(current, packed)
        assertEquals("needs", merged.id)
        assertEquals("Needs", merged.name)
        assertEquals(50, merged.percentage)
        assertEquals("#FF0000", merged.colorHex)
        assertEquals("#00FF00", merged.labelColorHex)
    }

    @Test
    fun mergeFundGoalLook_keepsAmounts() {
        val current = FundGoalConfig(name = "Liburan", targetAmount = 5_000_000.0, currentAmount = 200_000.0)
        val packed = FundGoalConfig(
            name = "Leaked",
            targetAmount = 99.0,
            currentAmount = 99.0,
            backgroundColorHex = "#123456"
        )
        val merged = ThemePackUtils.mergeFundGoalLook(current, packed)
        assertEquals("Liburan", merged.name)
        assertEquals(5_000_000.0, merged.targetAmount, 0.01)
        assertEquals(200_000.0, merged.currentAmount, 0.01)
        assertEquals("#123456", merged.backgroundColorHex)
    }

    @Test
    fun stripPrivateAmounts_zerosGoalMoney() {
        val payload = SharedCustomizationPayload(
            themeName = "Test",
            fundGoal = FundGoalConfig(name = "Rahasia", targetAmount = 9.0, currentAmount = 8.0)
        )
        val stripped = ThemePackUtils.stripPrivateAmounts(payload)
        assertEquals("Goal", stripped.fundGoal.name)
        assertEquals(0.0, stripped.fundGoal.targetAmount, 0.0)
        assertEquals(0.0, stripped.fundGoal.currentAmount, 0.0)
    }
}
