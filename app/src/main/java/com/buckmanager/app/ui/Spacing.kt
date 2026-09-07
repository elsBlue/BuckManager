package com.buckmanager.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 8dp Grid System for Buck Manager UI/UX Architecture
 * 
 * Consistent Spatial Baseline Guidelines:
 * - Micro/Tight Adjustments: 4.dp (0.5x unit)
 * - Base Grid Unit:          8.dp (1x unit)
 * - Compact Component Gap:   16.dp (1.5x unit)
 * - Standard Screen Padding:  16.dp (2x unit)
 * - Card/Section Margin:     24.dp (2.5x unit)
 * - Major Section Divider:   24.dp (3x unit)
 * - Hero/Top Screen Inset:   32.dp (4x unit)
 * - Accessible Touch Target: 48.dp (6x unit)
 * - Extra Large Spacer:      64.dp (8x unit)
 */
data class GridSpacing(
    val none: Dp = 0.dp,
    val micro: Dp = 4.dp,       // 0.5x - Micro spacing / badge padding
    val base: Dp = 8.dp,        // 1.0x - Primary grid unit / element gap
    val compact: Dp = 16.dp,    // 1.5x - Card internal spacing
    val standard: Dp = 16.dp,   // 2.0x - Standard screen padding / edge margin
    val medium: Dp = 24.dp,     // 2.5x - Medium section padding
    val large: Dp = 24.dp,      // 3.0x - Major section divider / card gap
    val xLarge: Dp = 32.dp,     // 4.0x - Screen top/bottom inset
    val touchTarget: Dp = 48.dp, // 6.0x - Minimum touch target dimension
    val xxLarge: Dp = 64.dp     // 8.0x - Hero spacing / prominent headers
)

val LocalSpacing = compositionLocalOf { GridSpacing() }

/**
 * Convenient shortcut accessor for Theme Spacing
 */
val AppSpacing: GridSpacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current
