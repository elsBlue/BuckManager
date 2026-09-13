package com.buckmanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buckmanager.app.model.CurrencyConfig
import com.buckmanager.app.ui.AppChrome
import com.buckmanager.app.ui.AppShape
import com.buckmanager.app.ui.GoldAccent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

data class FeatureItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val highlightWord: String = ""
)

val features = listOf(
    FeatureItem(
        title = "Smart Categorization",
        description = "Easily group your expenses and incomes into custom envelopes.",
        icon = Icons.Default.AutoAwesome,
        highlightWord = "envelopes"
    ),
    FeatureItem(
        title = "Rich Customizability",
        description = "Make it yours! Change themes, headers, and backgrounds effortlessly.",
        icon = Icons.Default.ColorLens,
        highlightWord = "Make it yours"
    )
)

@Composable
fun OnboardingScreen(isDarkMode: Boolean, onFinish: (String, String) -> Unit) {
    val bgColor = if (isDarkMode) AppChrome.pageDark else AppChrome.pageLight
    val surfaceColor = if (isDarkMode) AppChrome.rowDark else AppChrome.rowLight
    val textColor = if (isDarkMode) Color.White else Color(0xFF121926)
    val subtitleColor = if (isDarkMode) Color(0xFFA0A0AB) else Color(0xFF5A667A)
    var selectedCode by remember { mutableStateOf(CurrencyConfig.currencyCode.ifBlank { "IDR" }) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.3f))
            
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(surfaceColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = buildAnnotatedString {
                    append("Welcome to\n")
                    withStyle(SpanStyle(color = GoldAccent)) {
                        append("Buck Manager")
                    }
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Your ultimate companion for managing finances with style and precision.",
                fontSize = 15.sp,
                color = subtitleColor,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                features.forEach { feature ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(AppShape.chip))
                                .background(surfaceColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = feature.icon,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(14.dp))
                        
                        Column {
                            Text(
                                text = feature.title,
                                color = textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = buildAnnotatedString {
                                    val parts = feature.description.split(feature.highlightWord)
                                    if (parts.size == 2 && feature.highlightWord.isNotEmpty()) {
                                        append(parts[0])
                                        withStyle(SpanStyle(color = GoldAccent)) {
                                            append(feature.highlightWord)
                                        }
                                        append(parts[1])
                                    } else {
                                        append(feature.description)
                                    }
                                },
                                color = subtitleColor,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Your currency",
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "IDR is the default. You can change this later in Menu.",
                color = subtitleColor,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurrencyConfig.choices.forEach { choice ->
                    val selected = selectedCode == choice.code
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) GoldAccent else surfaceColor)
                            .border(
                                1.dp,
                                if (selected) GoldAccent else (if (isDarkMode) Color(0xFF2A273C) else Color(0xFFCBD5E1)),
                                RoundedCornerShape(50)
                            )
                            .clickable { selectedCode = choice.code }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                choice.code,
                                color = if (selected) Color.White else textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                choice.symbol.trim(),
                                color = if (selected) Color.White.copy(alpha = 0.9f) else subtitleColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    val choice = CurrencyConfig.choices.firstOrNull { it.code == selectedCode }
                        ?: CurrencyConfig.choices.first()
                    onFinish(choice.code, choice.symbol)
                },
                shape = RoundedCornerShape(AppShape.button),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Get Started",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
