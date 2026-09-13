package com.buckmanager.app.model

import android.content.Context

data class CurrencyChoice(
    val code: String,
    val title: String,
    val symbol: String
)

object CurrencyConfig {
    var currencyCode: String = "IDR"
    var symbol: String = "Rp "

    val choices: List<CurrencyChoice> = listOf(
        CurrencyChoice("IDR", "Indonesian Rupiah (IDR)", "Rp "),
        CurrencyChoice("USD", "United States Dollar (USD)", "$"),
        CurrencyChoice("EUR", "Euro (EUR)", "€"),
        CurrencyChoice("GBP", "British Pound (GBP)", "£")
    )

    fun normalizeSymbol(code: String, sym: String): String = when (code) {
        "IDR" -> "Rp "
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        else -> if (sym.isBlank()) code else sym
    }

    fun load(context: Context) {
        val prefs = context.getSharedPreferences("buckmanager_prefs", Context.MODE_PRIVATE)
        currencyCode = prefs.getString("currency_code", "IDR") ?: "IDR"
        val stored = prefs.getString("currency_symbol", "Rp ") ?: "Rp "
        symbol = normalizeSymbol(currencyCode, stored)
        if (symbol != stored) {
            prefs.edit().putString("currency_symbol", symbol).apply()
        }
    }

    fun save(context: Context, code: String, sym: String) {
        currencyCode = code
        symbol = normalizeSymbol(code, sym)
        context.getSharedPreferences("buckmanager_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("currency_code", code)
            .putString("currency_symbol", symbol)
            .apply()
    }
}
