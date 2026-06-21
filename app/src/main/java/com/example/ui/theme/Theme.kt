package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryTechLight,
    secondary = Color(0xFF82B1FF),
    tertiary = Color(0xFFFFB74D),
    background = Color(0xFF0B0E1B),
    surface = Color(0xFF161A30),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF060812),
    onSurfaceVariant = Color(0xFF8B949E)
)

private val LightColorScheme = lightColorScheme(
    primary = BentoBluePrimary,
    secondary = SecondaryCloudBlue,
    tertiary = TertiaryWarmOrange,
    background = BentoBg,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = BentoTextPrimary,
    onSurface = BentoTextPrimary,
    surfaceVariant = BentoGrayLight,
    onSurfaceVariant = BentoTextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // We enforce darkTheme to match the "developer dark terminal coding" experience,
    // but if the user explicitly prefers light mode, we can honor it or keep it styled. Let's support both.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
