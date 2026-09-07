package com.buckmanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF3673FC)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "B",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BUCK MANAGER",
                    color = appNameColor.copy(alpha = 0.5f),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = title,
                    color = titleDisplayColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Clip
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (showHideBalances) {
                HeaderIconChip(
                    onClick = onToggleHideBalances,
                    imageVector = if (hideBalances) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Toggle Balance Visibility",
                    tint = if (hideBalances) Color(0xFF9CA3AF) else Color(0xFF3673FC),
                    background = buttonBg
                )
            }

            if (showUnlockCustomization) {
                if (!hasPremium) {
                    HeaderIconChip(
                        onClick = onPremiumClick,
                        imageVector = Icons.Default.Star,
                        contentDescription = "Unlock customization",
                        tint = Color(0xFFFCBF36),
                        background = buttonBg
                    )
                } else {
                    HeaderIconChip(
                        onClick = onToggleLock,
                        imageVector = if (isEditLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Toggle Lock",
                        tint = Color(0xFFFCBF36),
                        background = buttonBg
                    )
                }
            }

            if (showUnlockCustomization && hasPremium && !isEditLocked && onCustomizeClick != null) {
                HeaderIconChip(
                    onClick = onCustomizeClick,
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Customize Background",
                    tint = GoldAccent,
                    background = buttonBg
                )
            }
        }
    }
}

@Composable
private fun HeaderIconChip(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String,
    tint: Color,
    background: Color
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}
