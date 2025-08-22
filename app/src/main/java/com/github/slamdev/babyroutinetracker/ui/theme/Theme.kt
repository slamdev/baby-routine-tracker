package com.github.slamdev.babyroutinetracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    // Improved dark color scheme with better surface hierarchy
    background = Color(0xFF121212), // True Material dark background
    surface = Color(0xFF1E1E1E), // Elevated surface for cards
    surfaceVariant = Color(0xFF2C2C2C), // For elevated components
    onPrimary = Color(0xFF1C1B1F),
    onSecondary = Color(0xFF1C1B1F),
    onTertiary = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE3E3E3), // Better contrast
    onSurface = Color(0xFFE3E3E3),
    // Success/error colors for dark mode
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    outline = Color(0xFF3C3C3C) // Subtle borders
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    // Improved light color scheme with better surface hierarchy
    background = Color(0xFFFCFCFC), // Softer than pure white
    surface = Color(0xFFFFFFFF), // Pure white for cards
    surfaceVariant = Color(0xFFF5F5F5), // For elevated components
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1A1A1A), // Better contrast
    onSurface = Color(0xFF1A1A1A),
    // Success/error colors for light mode
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    outline = Color(0xFFE0E0E0) // Subtle borders
)

@Composable
fun BabyroutinetrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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

    // Provide extended colors via CompositionLocal
    val extendedColors = getExtendedColors(darkTheme)

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}