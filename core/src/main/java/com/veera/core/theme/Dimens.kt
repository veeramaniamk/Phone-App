package com.veera.core.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppDimensions(
    val paddingSmall: Dp = 8.dp,
    val paddingMedium: Dp = 16.dp,
    val paddingLarge: Dp = 24.dp,
    val paddingExtraLarge: Dp = 32.dp,
    
    val cornerSmall: Dp = 4.dp,
    val cornerMedium: Dp = 8.dp,
    val cornerLarge: Dp = 16.dp,
    val cornerExtraLarge: Dp = 24.dp,
    
    val buttonHeight: Dp = 48.dp,
    val iconSizeHeader: Dp = 24.dp,
    val cardElevation: Dp = 4.dp,
    
    val gridSpacing: Dp = 16.dp
)

val CompactDimensions = AppDimensions(
    paddingSmall = 4.dp,
    paddingMedium = 8.dp,
    paddingLarge = 16.dp,
    paddingExtraLarge = 24.dp,
    buttonHeight = 44.dp
)

val MediumDimensions = AppDimensions(
    paddingSmall = 8.dp,
    paddingMedium = 16.dp,
    paddingLarge = 24.dp,
    paddingExtraLarge = 32.dp,
    buttonHeight = 56.dp
)

val ExpandedDimensions = AppDimensions(
    paddingSmall = 12.dp,
    paddingMedium = 24.dp,
    paddingLarge = 32.dp,
    paddingExtraLarge = 48.dp,
    buttonHeight = 64.dp
)

val LocalAppDimensions = compositionLocalOf { AppDimensions() }
