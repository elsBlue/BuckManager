package com.buckmanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buckmanager.app.model.CurrencyConfig
import com.buckmanager.app.ui.AppShape

@Composable
fun CurrencyModal(
    visible: Boolean,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onSelect: (String, String) -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDarkMode) Color(0xFF1E1B2E) else Color(0xFFF4F5F9),
        title = {
            Text("Select Currency", color = if (isDarkMode) Color.White else Color.Black, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CurrencyOption(
                    title = "Indonesian Rupiah (IDR)",
                    symbol = "Rp",
                    isSelected = CurrencyConfig.currencyCode == "IDR",
                    isDarkMode = isDarkMode,
                    onClick = { onSelect("IDR", "Rp") }
                )
                CurrencyOption(
                    title = "United States Dollar (USD)",
                    symbol = "$",
                    isSelected = CurrencyConfig.currencyCode == "USD",
                    isDarkMode = isDarkMode,
                    onClick = { onSelect("USD", "$") }
                )
                CurrencyOption(
                    title = "Euro (EUR)",
                    symbol = "€",
                    isSelected = CurrencyConfig.currencyCode == "EUR",
                    isDarkMode = isDarkMode,
                    onClick = { onSelect("EUR", "€") }
                )
                CurrencyOption(
                    title = "British Pound (GBP)",
                    symbol = "£",
                    isSelected = CurrencyConfig.currencyCode == "GBP",
                    isDarkMode = isDarkMode,
                    onClick = { onSelect("GBP", "£") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFFD4A54A))
            }
        }
    )
}

@Composable
private fun CurrencyOption(title: String, symbol: String, isSelected: Boolean, isDarkMode: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShape.card))
            .background(if (isSelected) Color(0xFFD4A54A).copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = if (isDarkMode) Color.White else Color.Black, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        Text(symbol, color = if (isSelected) Color(0xFFD4A54A) else (if (isDarkMode) Color(0xFF8B92A5) else Color(0xFF5A667A)), fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
