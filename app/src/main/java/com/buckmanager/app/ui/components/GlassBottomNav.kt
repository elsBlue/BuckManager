package com.buckmanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.buckmanager.app.ui.AppShape
import com.buckmanager.app.ui.AppStroke

@Composable
fun GlassBottomNav(
    currentRoute: String,
    canvasColor: Color,
    textColor: Color,
    hasPhoto: Boolean,
    dimOpacity: Int,
    onDashboard: () -> Unit,
    onTransactions: () -> Unit,
    onAdd: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val darkSurface = navSurfaceIsDark(canvasColor, textColor, hasPhoto, dimOpacity)
    val activeTint = if (darkSurface) Color.White else Color(0xFF0F172A)
    val idleTint = activeTint.copy(alpha = 0.42f)
    val shape = RoundedCornerShape(AppShape.panel)
    val frost = if (darkSurface) {
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.07f))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.62f), Color.White.copy(alpha = 0.38f))
        )
    }
    val stroke = if (darkSurface) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.7f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .shadow(12.dp, shape, ambientColor = Color.Black.copy(alpha = 0.18f), spotColor = Color.Black.copy(alpha = 0.18f))
                .clip(shape)
                .background(frost)
                .border(AppStroke.thin, stroke, shape)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavIcon(
                    selected = currentRoute == "dashboard",
                    selectedIcon = Icons.Filled.Home,
                    idleIcon = Icons.Outlined.Home,
                    label = "Dashboard",
                    activeTint = activeTint,
                    idleTint = idleTint,
                    onClick = onDashboard,
                    modifier = Modifier.weight(1f)
                )
                NavIcon(
                    selected = currentRoute == "transactions",
                    selectedIcon = Icons.Filled.History,
                    idleIcon = Icons.Filled.History,
                    label = "Transactions",
                    activeTint = activeTint,
                    idleTint = idleTint,
                    onClick = onTransactions,
                    modifier = Modifier.weight(1f)
                )
                NavIcon(
                    selected = false,
                    selectedIcon = Icons.Default.Menu,
                    idleIcon = Icons.Default.Menu,
                    label = "Menu",
                    activeTint = activeTint,
                    idleTint = idleTint,
                    onClick = onSettings,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(58.dp)
                .shadow(
                    10.dp,
                    CircleShape,
                    ambientColor = Color(0xFFFCBF36).copy(alpha = 0.35f),
                    spotColor = Color(0xFFFCBF36).copy(alpha = 0.4f)
                )
                .clip(CircleShape)
                .background(Color(0xFFFCBF36))
                .clickable(onClick = onAdd),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add transaction",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

fun navSurfaceIsDark(
    canvasColor: Color,
    textColor: Color,
    hasPhoto: Boolean,
    dimOpacity: Int
): Boolean {
    if (hasPhoto) {
        if (dimOpacity >= 35) return true
        return textColor.luminance() > 0.5f
    }
    return canvasColor.luminance() < 0.45f
}

@Composable
private fun NavIcon(
    selected: Boolean,
    selectedIcon: ImageVector,
    idleIcon: ImageVector,
    label: String,
    activeTint: Color,
    idleTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (selected) activeTint else idleTint
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (selected) selectedIcon else idleIcon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(26.dp)
        )
    }
}
