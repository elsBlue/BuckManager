package com.buckmanager.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buckmanager.app.model.MonetizationState
import com.buckmanager.app.model.formatRp

@Composable
fun PremiumModal(
    visible: Boolean,
    isDarkMode: Boolean,
    monetizationState: MonetizationState,
    purchasePriceLabel: String? = null,
    onDismiss: () -> Unit,
    onPurchase: () -> Unit,
    onRestorePurchases: () -> Unit = {},
    onEnableTestPremium: () -> Unit = {}
) {
    if (!visible) return

    val containerColor = if (isDarkMode) Color(0xFF1E1B2E) else Color(0xFFF4F5F9)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val muted = if (isDarkMode) Color(0xFF8B92A5) else Color(0xFF5A667A)
    val goldColor = Color(0xFFD4A54A)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = containerColor,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = "Premium", tint = goldColor, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buck Manager Premium", color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Lifetime unlock for card themes: colors, photos, radius, borders, padding, and particle backgrounds.",
                    color = muted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                if (monetizationState.isPremium) {
                    Text(
                        "Lifetime Premium is active.\nTap the gold lock, then the pencil on any card to customize.",
                        color = Color(0xFF4ECB8D),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                } else if (monetizationState.premiumExpiryDate > System.currentTimeMillis()) {
                    Text(
                        "Temporary access is active. Enjoy the editors.",
                        color = goldColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Included", color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("• Per-card and envelope themes", color = muted, fontSize = 12.sp)
                        Text("• Photo backgrounds and particles", color = muted, fontSize = 12.sp)
                        Text("• Unlock once, keep it on this Play account", color = muted, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Not included yet: Google Drive sync.", color = muted, fontSize = 11.sp)
                    }

                    Button(
                        onClick = onPurchase,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = goldColor),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "Buy Lifetime Premium · ${purchasePriceLabel ?: formatRp(15000.0)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(onClick = onRestorePurchases) {
                        Text("Restore purchases", color = goldColor, fontWeight = FontWeight.Bold)
                    }

                    if (com.buckmanager.app.BuildConfig.DEBUG) {
                        TextButton(onClick = onEnableTestPremium) {
                            Text("Use Test Premium", color = goldColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = muted)
            }
        }
    )
}
