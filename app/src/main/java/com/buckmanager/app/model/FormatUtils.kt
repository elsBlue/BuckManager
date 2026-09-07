package com.buckmanager.app.model

import kotlin.math.abs

fun formatRp(amount: Double): String {
    val locale = if (CurrencyConfig.currencyCode == "IDR") java.util.Locale("id", "ID") else java.util.Locale("en", "US")
    val formatter = java.text.NumberFormat.getNumberInstance(locale)
    formatter.maximumFractionDigits = 0
    val absFormatted = formatter.format(abs(amount))
    return if (amount < 0) "-${CurrencyConfig.symbol}$absFormatted" else "${CurrencyConfig.symbol}$absFormatted"
}
