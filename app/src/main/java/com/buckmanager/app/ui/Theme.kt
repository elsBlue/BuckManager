package com.buckmanager.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

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

@Composable
fun BuckManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Default to system theme, but controllable
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalSpacing provides GridSpacing()) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

