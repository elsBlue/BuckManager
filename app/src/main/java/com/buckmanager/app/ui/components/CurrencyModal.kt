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
import com.buckmanager.app.model.CurrencyChoice
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
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CurrencyConfig.choices.forEach { choice ->
                    CurrencyOption(
                        choice = choice,
                        isSelected = CurrencyConfig.currencyCode == choice.code,
                        isDarkMode = isDarkMode,
                        onClick = { onSelect(choice.code, choice.symbol) }
                    )
                }
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
private fun CurrencyOption(choice: CurrencyChoice, isSelected: Boolean, isDarkMode: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShape.card))
            .background(if (isSelected) Color(0xFFD4A54A).copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(choice.title, color = if (isDarkMode) Color.White else Color.Black, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        Text(choice.symbol.trim(), color = if (isSelected) Color(0xFFD4A54A) else (if (isDarkMode) Color(0xFF8B92A5) else Color(0xFF5A667A)), fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
