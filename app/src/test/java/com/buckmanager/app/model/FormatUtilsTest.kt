package com.buckmanager.app.model

import org.junit.Assert.assertEquals
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
    fun normalizeSymbol_idrAlwaysHasTrailingSpace() {
        assertEquals("Rp ", CurrencyConfig.normalizeSymbol("IDR", "Rp"))
        assertEquals("Rp ", CurrencyConfig.normalizeSymbol("IDR", "Rp "))
        assertEquals("$", CurrencyConfig.normalizeSymbol("USD", "USD"))
        assertEquals("€", CurrencyConfig.normalizeSymbol("EUR", "EUR"))
        assertEquals("£", CurrencyConfig.normalizeSymbol("GBP", "GBP"))
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

    @Test
    fun formatGroupedDigits_idrUsesDots() {
        assertEquals("", formatGroupedDigits(""))
        assertEquals("1", formatGroupedDigits("1"))
        assertEquals("12", formatGroupedDigits("12"))
        assertEquals("123", formatGroupedDigits("123"))
        assertEquals("1.000", formatGroupedDigits("1000"))
        assertEquals("10.000.000", formatGroupedDigits("10000000"))
        assertEquals("10.000.000", formatGroupedDigits("10.000.000"))
    }

    @Test
    fun parseAmountInput_stripsGrouping() {
        assertEquals(0.0, parseAmountInput(""), 0.0)
        assertEquals(10000000.0, parseAmountInput("10.000.000"), 0.0)
        assertEquals(1000.0, parseAmountInput("1,000"), 0.0)
    }
}
