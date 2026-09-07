package com.buckmanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.buckmanager.app.ui.components.parseHexColor
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buckmanager.app.ui.GoldAccent

@Composable
fun UniversalHeader(
    title: String,
    hasPremium: Boolean = false,
    isEditLocked: Boolean = true,
    isDarkMode: Boolean = true,
    showUnlockCustomization: Boolean = true,
    hideBalances: Boolean = false,
    onToggleLock: () -> Unit = {},
    onCustomizeClick: (() -> Unit)? = null,
    onPremiumClick: () -> Unit = {},
    onToggleHideBalances: () -> Unit = {},
    showHideBalances: Boolean = true,
    textColorHex: String? = null,
    appNameColorHex: String? = null,
    titleColorHex: String? = null
) {
    val defaultTextColor = if (isDarkMode) Color.White else Color(0xFF121926)
    val appNameColor = appNameColorHex?.let { parseHexColor(it, defaultTextColor) } ?: textColorHex?.let { parseHexColor(it, defaultTextColor) } ?: defaultTextColor
    val titleDisplayColor = titleColorHex?.let { parseHexColor(it, defaultTextColor) } ?: textColorHex?.let { parseHexColor(it, defaultTextColor) } ?: defaultTextColor
    val buttonBg = if (isDarkMode) Color(0xFF231F33) else Color(0xFFE2E8F0)
    val iconTint = textColorHex?.let { parseHexColor(it, defaultTextColor) } ?: defaultTextColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF3673FC)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "B",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BUCK MANAGER",
                    color = appNameColor.copy(alpha = 0.5f),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.offset(y = 4.dp),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = title,
                    color = titleDisplayColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    modifier = Modifier.offset(y = (-4).dp),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Eye toggle for hide balances
            if (showHideBalances) {
                IconButton(
                    onClick = onToggleHideBalances,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(buttonBg)
                ) {
                    Icon(
                        imageVector = if (hideBalances) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Balance Visibility",
                        tint = if (hideBalances) Color(0xFF9CA3AF) else Color(0xFF3673FC),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (showUnlockCustomization) {
                if (!hasPremium) {
                    Surface(
                        onClick = onPremiumClick,
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.height(46.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxHeight().padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFCBF36), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Unlock\nCustomization",
                                color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Left,
                                lineHeight = 12.sp
                            )
                        }
                    }
                } else {
                    IconButton(
                        onClick = onToggleLock,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(buttonBg)
                    ) {
                        Icon(
                            imageVector = if (isEditLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Toggle Lock",
                            tint = Color(0xFFFCBF36),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            if (showUnlockCustomization && hasPremium && !isEditLocked && onCustomizeClick != null) {
                IconButton(
                    onClick = onCustomizeClick,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(buttonBg)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Customize Background",
                        tint = GoldAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
