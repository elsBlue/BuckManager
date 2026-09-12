package com.buckmanager.app.model

import kotlin.math.abs

private const val MAX_AMOUNT_DIGITS = 12

fun groupingLocale(): java.util.Locale =
    if (CurrencyConfig.currencyCode == "IDR") java.util.Locale("id", "ID") else java.util.Locale("en", "US")

fun groupingSeparator(): Char = if (CurrencyConfig.currencyCode == "IDR") '.' else ','

fun formatRp(amount: Double): String {
    val formatter = java.text.NumberFormat.getNumberInstance(groupingLocale())
    formatter.maximumFractionDigits = 0
    val absFormatted = formatter.format(abs(amount))
    return if (amount < 0) "-${CurrencyConfig.symbol}$absFormatted" else "${CurrencyConfig.symbol}$absFormatted"
}

/** Digits-only string → grouped display, e.g. 10000000 → 10.000.000 (IDR). */
fun formatGroupedDigits(digits: String): String {
    val cleaned = digits.filter { it.isDigit() }.take(MAX_AMOUNT_DIGITS)
    if (cleaned.isEmpty()) return ""
    val sep = groupingSeparator()
    val sb = StringBuilder()
    val lead = cleaned.length % 3
    if (lead > 0) sb.append(cleaned, 0, lead)
    var i = lead
    while (i < cleaned.length) {
        if (sb.isNotEmpty()) sb.append(sep)
        sb.append(cleaned, i, i + 3)
        i += 3
    }
    return sb.toString()
}

fun filterAmountDigits(raw: String): String = raw.filter { it.isDigit() }.take(MAX_AMOUNT_DIGITS)

fun parseAmountInput(formatted: String): Double =
    formatted.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
