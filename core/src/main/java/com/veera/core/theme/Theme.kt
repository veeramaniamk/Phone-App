package com.veera.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.DarkBackground,
    secondary = AppColors.Secondary,
    tertiary = AppColors.Tertiary,
    background = AppColors.DarkBackground,
    surface = AppColors.DarkSurface,
    onBackground = AppColors.DarkOnBackground,
    onSurface = AppColors.DarkOnSurface,
    error = AppColors.Error
)

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.LightBackground,
    secondary = AppColors.Secondary,
    tertiary = AppColors.Tertiary,
    background = AppColors.LightBackground,
    surface = AppColors.LightSurface,
    onBackground = AppColors.LightOnBackground,
    onSurface = AppColors.LightOnSurface,
    error = AppColors.Error
)

@Composable
fun DialerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val configuration = LocalConfiguration.current
    
    // Determine screen size class based on width and height
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp
    
    val isSmallHeight = screenHeight < 480
    
    val dimensions = when {
        screenWidth < 600 -> if (isSmallHeight) CompactDimensions.copy(paddingMedium = 4.dp, paddingLarge = 8.dp) else CompactDimensions
        screenWidth < 840 -> MediumDimensions
        else -> ExpandedDimensions
    }
    
    val typography = when {
        screenWidth < 600 -> CompactTypography
        screenWidth < 840 -> MediumTypography
        else -> ExpandedTypography
    }

    CompositionLocalProvider(
        LocalAppDimensions provides dimensions,
        LocalAppTypography provides typography
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography.toMaterial3(),
            content = content
        )
    }
}

/**
 * Helper object to access the current theme properties
 */
object AppTheme {
    val dimensions: AppDimensions
        @Composable
        get() = LocalAppDimensions.current
        
    val typography: AppTypography
        @Composable
        get() = LocalAppTypography.current
        
    val colors = AppColors
}
