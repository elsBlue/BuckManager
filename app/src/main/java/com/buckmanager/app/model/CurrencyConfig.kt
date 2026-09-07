package com.buckmanager.app.model

import android.content.Context

object CurrencyConfig {
    var currencyCode: String = "IDR"
    var symbol: String = "Rp "

    fun load(context: Context) {
        val prefs = context.getSharedPreferences("buckmanager_prefs", Context.MODE_PRIVATE)
        currencyCode = prefs.getString("currency_code", "IDR") ?: "IDR"
        symbol = prefs.getString("currency_symbol", "Rp ") ?: "Rp "
    }

    fun save(context: Context, code: String, sym: String) {
        currencyCode = code
        symbol = sym
        context.getSharedPreferences("buckmanager_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("currency_code", code)
            .putString("currency_symbol", sym)
            .apply()
    }
}
