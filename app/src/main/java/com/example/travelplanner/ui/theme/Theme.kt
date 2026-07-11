package com.example.travelplanner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(

    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE7FF),
    onPrimaryContainer = LightTextPrimary,

    secondary = TealSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5F5F2),
    onSecondaryContainer = LightTextPrimary,

    tertiary = OrangeCTA,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE3D3),
    onTertiaryContainer = LightTextPrimary,

    background = LightBackground,
    onBackground = LightTextPrimary,

    surface = LightSurface,
    onSurface = LightTextPrimary,

    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = LightTextSecondary,

    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color(0xFFF8FAFC),

    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),

    error = Color(0xFFDC2626),
    onError = Color.White
)

private val DarkColors = darkColorScheme(

    // Orange becomes the main accent in Dark Mode
    primary = OrangeCTADark,
    onPrimary = Color.Black,

    primaryContainer = Color(0xFF7C2D12),
    onPrimaryContainer = Color.White,

    secondary = TealSecondaryDark,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF115E59),
    onSecondaryContainer = Color.White,

    tertiary = OrangeCTADark,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF7C2D12),
    onTertiaryContainer = Color.White,

    background = DarkBackground,
    onBackground = DarkTextPrimary,

    surface = DarkSurface,
    onSurface = DarkTextPrimary,

    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = DarkTextSecondary,

    surfaceContainer = DarkSurface,
    surfaceContainerLow = DarkSurface,
    surfaceContainerHigh = DarkSurface,
    surfaceContainerHighest = Color(0xFF273548),

    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155),

    error = Color(0xFFEF4444),
    onError = Color.White
)

@Composable
fun TravelPlannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}