package com.github.slamdev.babyroutinetracker.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extended colors that aren't part of Material 3 ColorScheme
 * but are needed for our app's success states and activity buttons
 */
@Stable
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    // Action button colors for activity cards
    val actionButton: Color,
    val onActionButton: Color,
    val actionButtonPressed: Color,
    // Elevated surface for better card contrast
    val surfaceElevated: Color
)

/**
 * Light theme extended colors
 */
private val LightExtendedColors = ExtendedColors(
    success = SuccessLight,
    onSuccess = OnSuccessLight,
    successContainer = SuccessContainerLight,
    onSuccessContainer = OnSuccessContainerLight,
    // Modern action button colors for light theme
    actionButton = Color(0xFF6366F1), // Modern indigo (more interesting than grey)
    onActionButton = Color.White,
    actionButtonPressed = Color(0xFF4F46E5), // Darker indigo for pressed state
    surfaceElevated = Color(0xFFFAFAFA) // Slightly elevated surface
)

/**
 * Dark theme extended colors
 */
private val DarkExtendedColors = ExtendedColors(
    success = SuccessDark,
    onSuccess = OnSuccessDark,
    successContainer = SuccessContainerDark,
    onSuccessContainer = OnSuccessContainerDark,
    // Harmonious action button colors for dark theme
    actionButton = Color(0xFF4C51BF), // Deeper indigo that works with dark palette
    onActionButton = Color(0xFFE5E7EB),
    actionButtonPressed = Color(0xFF3730A3), // Even deeper for pressed state
    surfaceElevated = Color(0xFF262626) // Elevated surface for dark theme
)

/**
 * Composition local for extended colors
 */
val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

/**
 * Get extended colors based on current theme
 */
@Composable
fun getExtendedColors(isDarkTheme: Boolean): ExtendedColors {
    return if (isDarkTheme) DarkExtendedColors else LightExtendedColors
}

/**
 * Extension property to access extended colors from MaterialTheme
 */
val ColorScheme.extended: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current
