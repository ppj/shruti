package com.hindustani.pitchdetector.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Light color scheme inspired by Shruti logo
 * Features deep blues, warm golds, and terracotta tones
 */
private val LightColorScheme = lightColorScheme(
    primary = DeepBlue,
    onPrimary = Color.White,
    primaryContainer = LightBlue,
    onPrimaryContainer = Color.White,

    secondary = WarmGold,
    onSecondary = TextDark,
    secondaryContainer = LightGold,
    onSecondaryContainer = TextDark,

    tertiary = Terracotta,
    onTertiary = Color.White,
    tertiaryContainer = WarmOrange,
    onTertiaryContainer = TextDark,

    background = WarmWhite,
    onBackground = TextDark,

    surface = OffWhite,
    onSurface = TextDark,
    surfaceVariant = LightGold,
    onSurfaceVariant = TextDark,

    outline = DeepBlue.copy(alpha = 0.5f),
    outlineVariant = WarmGold.copy(alpha = 0.3f)
)

/**
 * Dark color scheme inspired by Shruti logo
 * Inverted palette with gold accents on dark navy background
 */
private val DarkColorScheme = darkColorScheme(
    primary = BrightGold,
    onPrimary = TextDark,
    primaryContainer = DarkNavy,
    onPrimaryContainer = TextLight,

    secondary = WarmGold,
    onSecondary = TextDark,
    secondaryContainer = DarkGold,
    onSecondaryContainer = TextLight,

    tertiary = WarmOrange,
    onTertiary = TextDark,
    tertiaryContainer = Terracotta,
    onTertiaryContainer = Color.White,

    background = DarkBackground,
    onBackground = TextLight,

    surface = DarkSurface,
    onSurface = TextLight,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextLight,

    outline = BrightGold.copy(alpha = 0.5f),
    outlineVariant = WarmGold.copy(alpha = 0.3f)
)

/**
 * Shruti theme with support for light and dark modes
 * Color palette inspired by the app logo featuring tanpura, mandala, and sound waves
 *
 * @param darkTheme Whether to use dark theme (defaults to system setting)
 * @param dynamicColor Whether to use dynamic theming on Android 12+ (disabled by default to preserve branding)
 */
@Composable
fun ShrutiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
