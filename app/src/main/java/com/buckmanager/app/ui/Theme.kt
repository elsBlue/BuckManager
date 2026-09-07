package com.buckmanager.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

val GoldAccent = Color(0xFFF5B041)
val CobaltBlue = Color(0xFF1D2A96)
val CoralPink = Color(0xFFEC407A)
val SunGold = Color(0xFFF5B041)
val DarkBackground = Color(0xFF0F1117)
val CardBackground = Color(0xFF181C26)
val NeedsColor = Color(0xFFFCBF36)
val WantsColor = Color(0xFF38BDF8)
val SavingsColor = Color(0xFFF5B041)
val ExpenseRed = Color(0xFFFB7185)
val TextDim = Color(0xFF9CA3AF)

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    secondary = WantsColor,
    tertiary = SavingsColor,
    background = DarkBackground,
    surface = CardBackground,
    onPrimary = Color(0xFF0F1117),
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = GoldAccent,
    secondary = WantsColor,
    tertiary = SavingsColor,
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onBackground = Color(0xFF121926),
    onSurface = Color(0xFF121926)
)

private val CompactLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.Both
)

private fun TextStyle.compact(lineHeightMultiplier: Float = 1.28f): TextStyle {
    val nextLineHeight = if (fontSize.isSp) (fontSize.value * lineHeightMultiplier).sp else lineHeight
    return copy(
        lineHeight = nextLineHeight,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = CompactLineHeight
    )
}

private val CompactTypography: Typography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.compact(1.15f),
        displayMedium = base.displayMedium.compact(1.15f),
        displaySmall = base.displaySmall.compact(1.2f),
        headlineLarge = base.headlineLarge.compact(1.2f),
        headlineMedium = base.headlineMedium.compact(1.2f),
        headlineSmall = base.headlineSmall.compact(1.22f),
        titleLarge = base.titleLarge.compact(1.2f),
        titleMedium = base.titleMedium.compact(1.22f),
        titleSmall = base.titleSmall.compact(1.22f),
        bodyLarge = base.bodyLarge.compact(1.3f),
        bodyMedium = base.bodyMedium.compact(1.3f),
        bodySmall = base.bodySmall.compact(1.3f),
        labelLarge = base.labelLarge.compact(1.2f),
        labelMedium = base.labelMedium.compact(1.2f),
        labelSmall = base.labelSmall.compact(1.2f)
    )
}

/** Inherited by raw Text() calls that only set fontSize — otherwise they keep bodyLarge's 24sp line height. */
private val CompactDefaultTextStyle = TextStyle(
    lineHeight = TextUnit.Unspecified,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = CompactLineHeight
)

@Composable
fun BuckManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CompactTypography
    ) {
        CompositionLocalProvider(
            LocalSpacing provides GridSpacing(),
            LocalTextStyle provides CompactDefaultTextStyle
        ) {
            content()
        }
    }
}
