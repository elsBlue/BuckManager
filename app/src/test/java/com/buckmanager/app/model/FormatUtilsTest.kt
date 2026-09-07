package com.buckmanager.app.model

import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FormatUtilsTest {

    @Before
    fun setIdrDefaults() {
        CurrencyConfig.currencyCode = "IDR"
        CurrencyConfig.symbol = "Rp "
    }

    @Test
    fun formatRp_includesSymbolAndDigits() {
        val formatted = formatRp(15000.0)
        assertTrue(formatted.startsWith("Rp"))
        assertTrue(formatted.replace(".", "").replace(",", "").contains("15000") || formatted.contains("15"))
    }

    @Test
    fun formatRp_negativeUsesMinusPrefix() {
        val formatted = formatRp(-2500.0)
        assertTrue(formatted.startsWith("-Rp"))
    }
}
