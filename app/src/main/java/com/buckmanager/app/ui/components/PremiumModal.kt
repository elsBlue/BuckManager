package com.buckmanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.buckmanager.app.BuildConfig
import com.buckmanager.app.model.MonetizationState
import com.buckmanager.app.model.formatRp
import com.buckmanager.app.ui.AppShape
import com.buckmanager.app.ui.GoldAccent

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

    val sheetBg = if (isDarkMode) Color(0xFF181C26) else Color.White
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val muted = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B)
    val hairline = if (isDarkMode) Color(0xFF2A273C) else Color(0xFFE2E8F0)
    val rowBg = if (isDarkMode) Color(0xFF12151C) else Color(0xFFF6FAFD)
    val isPremium = monetizationState.isPremium

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .border(1.dp, hairline, RoundedCornerShape(AppShape.panel)),
            shape = RoundedCornerShape(AppShape.panel),
            color = sheetBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isDarkMode) Color(0xFF12151C) else Color(0xFFF1F5F9))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = muted, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Premium", color = textColor, fontWeight = FontWeight.Black, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if (isPremium) "Lifetime is on. Customize any card."
                    else "Unlock looks for every card — once, forever on this Play account.",
                    color = muted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (isPremium) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AppShape.card))
                            .background(Color(0xFF34D399).copy(alpha = 0.12f))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF34D399)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Active", color = Color(0xFF059669), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                "Open the gold lock, then tap the pencil on a card.",
                                color = muted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(AppShape.button)
                    ) {
                        Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AppShape.card))
                            .background(rowBg)
                            .padding(vertical = 6.dp)
                    ) {
                        PremiumPerk("Card & envelope colors", textColor)
                        PremiumPerk("Photos, radius, borders, padding", textColor)
                        PremiumPerk("Particle backgrounds", textColor)
                        PremiumPerk("Lifetime on this Play account", textColor)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onPurchase,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(AppShape.button)
                    ) {
                        Text(
                            "Unlock lifetime  ·  ${purchasePriceLabel ?: formatRp(15000.0)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Restore purchase",
                        color = muted,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onRestorePurchases)
                            .padding(vertical = 6.dp)
                    )

                    if (BuildConfig.DEBUG) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Debug · test premium",
                            color = muted.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onEnableTestPremium)
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumPerk(label: String, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(GoldAccent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(13.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = textColor, fontWeight = FontWeight.Medium, fontSize = 13.sp, modifier = Modifier.weight(1f))
    }
}
